package com.asfoundation.wallet.repository

import com.asfoundation.wallet.entity.NetworkInfo
import com.asfoundation.wallet.interact.DefaultTokenProvider
import com.asfoundation.wallet.poa.BlockchainErrorMapper
import com.asfoundation.wallet.repository.entity.TransactionEntity
import com.asfoundation.wallet.service.AccountKeystoreService
import com.asfoundation.wallet.transactions.Transaction
import com.asfoundation.wallet.ui.iab.raiden.MultiWalletNonceObtainer
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.*
import java.util.concurrent.TimeUnit

class BackendTransactionRepository(
    networkInfo: NetworkInfo,
    accountKeystoreService: AccountKeystoreService,
    defaultTokenProvider: DefaultTokenProvider,
    errorMapper: BlockchainErrorMapper,
    nonceObtainer: MultiWalletNonceObtainer,
    scheduler: Scheduler,
    private val offChainTransactions: OffChainTransactions,
    private val localRepository: TransactionsRepository,
    private val mapper: TransactionMapper,
    private val disposables: CompositeDisposable,
    private val ioScheduler: Scheduler) :
    TransactionRepository(networkInfo, accountKeystoreService,
        defaultTokenProvider, errorMapper, nonceObtainer, scheduler) {

  private lateinit var disposable: Disposable
  override fun fetchTransaction(wallet: String): Observable<List<Transaction>> {
    if (!::disposable.isInitialized || disposable.isDisposed) {
      disposable = getLastProcessedTime(wallet)
          .subscribeOn(ioScheduler)
          .flatMapObservable { startingDate ->
            return@flatMapObservable Observable.merge(
                fetchNewTransactions(wallet, startingDate = startingDate),
                fetchMissingOldTransactions(wallet))
          }
          .buffer(2, TimeUnit.SECONDS)
          .doOnNext { localRepository.insertAll(it.flatten()) }
          .subscribe({}, { it.printStackTrace() })
    }
    disposables.add(disposable)

    return localRepository.getAllAsFlowable(wallet)
        .map { mapper.map(it) }
        .toObservable()
        .distinctUntilChanged()
  }

  override fun fetchNewTransactions(wallet: String): Single<List<Transaction>> {
    return localRepository.getNewestTransaction(wallet)
        .map { it.processedTime }
        .defaultIfEmpty(0)
        .subscribeOn(ioScheduler)
        .flatMapSingle { startingDate ->
          //We need +1 otherwise since the transaction on the backend is stored with 6 milliseconds
          // and we store with 3, so the last transaction will always be returned
          fetchNewTransactions(wallet, startingDate + 1).firstOrError()
        }
        .doOnSuccess { localRepository.insertAll(it) }
        .map { mapper.map(it) }
  }

  private fun fetchNewTransactions(wallet: String,
                                   startingDate: Long): Observable<MutableList<TransactionEntity>> {
    var sort = OffChainTransactions.Sort.DESC
    if (startingDate != 0L) {
      sort = OffChainTransactions.Sort.ASC
    }
    return fetchTransactions(wallet, startingDate = startingDate, sort = sort)
  }

  private fun fetchMissingOldTransactions(
      wallet: String): Observable<MutableList<TransactionEntity>> {
    return localRepository.isOldTransactionsLoaded()
        .flatMapObservable { isLoaded ->
          if (isLoaded) {
            return@flatMapObservable Observable.empty<MutableList<TransactionEntity>>()
          }
          return@flatMapObservable localRepository.getOlderTransaction(wallet)
              .map { it.processedTime }
              .flatMapObservable {
                fetchTransactions(wallet, 0L, it, OffChainTransactions.Sort.DESC)
              }
              .doOnComplete { localRepository.oldTransactionsLoaded() }
        }

  }

  private fun fetchTransactions(wallet: String,
                                startingDate: Long? = null,
                                endDate: Long? = null,
                                sort: OffChainTransactions.Sort? = null): Observable<MutableList<TransactionEntity>> {
    return TransactionsLoadObservable(offChainTransactions, wallet, startingDate, endDate, sort)
        .flatMapSingle { transactions ->
          Observable.fromIterable(transactions)
              .map { mapper.map(it, wallet) }
              .toList()
        }
  }

  private fun getLastProcessedTime(wallet: String): Maybe<Long> {
    val lastLocale = localRepository.getLastLocale()
    val currentLocale = Locale.getDefault().language
    return if (lastLocale == null || lastLocale == currentLocale) {
      if (lastLocale == null) localRepository.setLocale(currentLocale)
      localRepository.getNewestTransaction(wallet)
          .map { it.processedTime }
          .defaultIfEmpty(0)
    } else {
      Maybe.fromCallable {
        localRepository.setLocale(currentLocale)
        localRepository.deleteAllTransactions()
        0L
      }
    }
  }

  override fun stop() = disposables.clear()
}