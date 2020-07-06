package com.asfoundation.wallet.ui.iab

import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import io.reactivex.Observable

internal interface AppcoinsRewardsBuyView {
  fun finish(purchase: Purchase?)
  fun showLoading()
  fun hideLoading()
  fun showNoNetworkError()
  fun getOkErrorClick(): Observable<Any>
  fun getSupportIconClick(): Observable<Any>
  fun getSupportLogoClick(): Observable<Any>
  fun close()
  fun showGenericError()
  fun showError(message: Int?)
  fun finish(uid: String)
  fun errorClose()
  fun finish(purchase: Purchase, orderReference: String)
  fun showTransactionCompleted()
  fun getAnimationDuration(): Long
  fun lockRotation()
}