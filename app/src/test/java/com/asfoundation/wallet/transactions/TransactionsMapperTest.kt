package com.asfoundation.wallet.transactions

import com.asfoundation.wallet.entity.WalletHistory
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class TransactionsMapperTest {

  private lateinit var history: WalletHistory
  private lateinit var transactionMapper: TransactionsMapper

  @Before
  fun before() {
    //Json with a topup, topup normal bonus, perk bonus, iap and iap normal bonus
    val transactionsJson =
        "{\"result\":[{\"TxID\":\"0xfec381f4943569add55b55cd75bef5308a1c843b28aa5b5fc75f3bd2be6dacc9\",\"amount\":115000000000000000,\"app\":\"Appcoins Trivial Drive demo sample\",\"block\":0,\"bonus\":11.5,\"description\":null,\"fee\":0,\"icon\":\"https://apichain-dev.blockchainds.com/appc/icons/bonus.png\",\"icon_small\":null,\"operations\":[],\"perk\":null,\"processed_time\":\"2020-08-27 14:52:45.442269+0000\",\"receiver\":\"0x1bbbed2930395229b8f20fe1bad356b50a6b3f6f\",\"sender\":\"0x31a16adf2d5fc73f149fbb779d20c036678b1bbd\",\"sku\":\"gas\",\"status\":\"SUCCESS\",\"subtype\":null,\"title\":null,\"ts\":\"2020-08-27 14:52:45.295586+0000\",\"type\":\"bonus\"},{\"TxID\":\"0xa96d4160232abbdb0c8b0e43b941a679005dfe178fd0726d9836c47e057d8223\",\"amount\":1000000000000000000,\"app\":\"Appcoins Trivial Drive demo sample\",\"block\":0,\"bonus\":null,\"description\":null,\"fee\":0,\"icon\":\"https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png\",\"icon_small\":\"https://apichain-dev.blockchainds.com/appc/icons/iap_hybrid.png\",\"operations\":[],\"perk\":null,\"processed_time\":\"2020-08-27 14:52:45.154482+0000\",\"receiver\":\"0x123c2124b7f2c18b502296ba884d9cde201f1c32\",\"sender\":\"0x1bbbed2930395229b8f20fe1bad356b50a6b3f6f\",\"sku\":\"gas\",\"status\":\"SUCCESS\",\"subtype\":null,\"title\":null,\"ts\":\"2020-08-27 14:52:44.982794+0000\",\"type\":\"IAP OffChain\"},{\"TxID\":\"0x1120fb31a2cbe20a11c8251770ea53f9ee61d190919e91fe48c944c7a0aca612\",\"amount\":11000000000000000000,\"app\":null,\"block\":0,\"bonus\":null,\"description\":\"You will receive APPC-C when you reach a new gamification level\",\"fee\":0,\"icon\":\"https://apichain-dev.blockchainds.com/appc/icons/bonus.png\",\"icon_small\":null,\"operations\":[],\"perk\":\"GAMIFICATION_LEVEL_UP\",\"processed_time\":\"2020-08-27 14:32:32.844272+0000\",\"receiver\":\"0x1bbbed2930395229b8f20fe1bad356b50a6b3f6f\",\"sender\":\"0x31a16adf2d5fc73f149fbb779d20c036678b1bbd\",\"sku\":null,\"status\":\"SUCCESS\",\"subtype\":\"perk_bonus\",\"title\":\"Level Up Perk\",\"ts\":\"2020-08-27 14:32:32.676590+0000\",\"type\":\"bonus\"},{\"TxID\":\"0x20a71a4c8dc44d797e5736163e66f1e9eb983b2a14473f3a78f94fe3a46e6525\",\"amount\":31282226238000000000,\"app\":null,\"block\":0,\"bonus\":10.0,\"description\":null,\"fee\":0,\"icon\":\"https://apichain-dev.blockchainds.com/appc/icons/bonus.png\",\"icon_small\":null,\"operations\":[],\"perk\":null,\"processed_time\":\"2020-08-27 14:32:32.776814+0000\",\"receiver\":\"0x1bbbed2930395229b8f20fe1bad356b50a6b3f6f\",\"sender\":\"0x31a16adf2d5fc73f149fbb779d20c036678b1bbd\",\"sku\":null,\"status\":\"SUCCESS\",\"subtype\":null,\"title\":null,\"ts\":\"2020-08-27 14:32:32.594474+0000\",\"type\":\"bonus\"},{\"TxID\":\"0x259cf0e3447814dd6d80a87ea6cb911f9435712466306e4d135011fb4c715933\",\"amount\":312822262380000000000,\"app\":null,\"block\":0,\"bonus\":null,\"description\":null,\"fee\":0,\"icon\":\"https://apichain-dev.blockchainds.com/appc/icons/topup.png\",\"icon_small\":null,\"operations\":[],\"perk\":null,\"processed_time\":\"2020-08-27 14:32:32.487951+0000\",\"receiver\":\"0x1bbbed2930395229b8f20fe1bad356b50a6b3f6f\",\"sender\":\"0x31a16adf2d5fc73f149fbb779d20c036678b1bbd\",\"sku\":null,\"status\":\"SUCCESS\",\"subtype\":null,\"title\":null,\"ts\":\"2020-08-27 14:32:32.444254+0000\",\"type\":\"Topup OffChain\"}]}\n"
    val objectMapper = ObjectMapper()
    val df: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    objectMapper.dateFormat = df
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    history = objectMapper.readValue(transactionsJson, WalletHistory::class.java)
    transactionMapper = TransactionsMapper()
  }

  @Test
  fun transactionMapTest() {
    val transactions = transactionMapper.mapTransactionsFromWalletHistory(history.result)
    Assert.assertEquals(5, transactions.size)
    val topUpTransaction =
        Transaction("0x259cf0e3447814dd6d80a87ea6cb911f9435712466306e4d135011fb4c715933",
            Transaction.TransactionType.TOP_UP, null, null, null, null, null, 1598535596254,
            1598535639951, Transaction.TransactionStatus.SUCCESS, "312822262380000000000",
            "0x31a16adf2d5fc73f149fbb779d20c036678b1bbd",
            "0x1bbbed2930395229b8f20fe1bad356b50a6b3f6f",
            TransactionDetails(null, TransactionDetails.Icon(TransactionDetails.Icon.Type.URL,
                "https://apichain-dev.blockchainds.com/appc/icons/topup.png"),
                null), "APPC", Collections.emptyList())
    val topUpBonusTransaction =
        Transaction("0x20a71a4c8dc44d797e5736163e66f1e9eb983b2a14473f3a78f94fe3a46e6525",
            Transaction.TransactionType.BONUS, null, null, null, null, null, 1598535746474,
            1598535928814, Transaction.TransactionStatus.SUCCESS, "31282226238000000000",
            "0x31a16adf2d5fc73f149fbb779d20c036678b1bbd",
            "0x1bbbed2930395229b8f20fe1bad356b50a6b3f6f",
            TransactionDetails("10", TransactionDetails.Icon(TransactionDetails.Icon.Type.URL,
                "https://apichain-dev.blockchainds.com/appc/icons/bonus.png"),
                null), "APPC", Collections.emptyList())
    val perkBonusTransaction =
        Transaction("0x1120fb31a2cbe20a11c8251770ea53f9ee61d190919e91fe48c944c7a0aca612",
            Transaction.TransactionType.BONUS, Transaction.SubType.PERK_PROMOTION, "Level Up Perk",
            "You will receive APPC-C when you reach a new gamification level",
            Transaction.Perk.GAMIFICATION_LEVEL_UP,
            null, 1598535828590,
            1598535996272, Transaction.TransactionStatus.SUCCESS, "11000000000000000000",
            "0x31a16adf2d5fc73f149fbb779d20c036678b1bbd",
            "0x1bbbed2930395229b8f20fe1bad356b50a6b3f6f",
            TransactionDetails(null, TransactionDetails.Icon(TransactionDetails.Icon.Type.URL,
                "https://apichain-dev.blockchainds.com/appc/icons/bonus.png"),
                null), "APPC", Collections.emptyList())
    val iapTransaction =
        Transaction("0xa96d4160232abbdb0c8b0e43b941a679005dfe178fd0726d9836c47e057d8223",
            Transaction.TransactionType.IAP_OFFCHAIN, null, null, null, null,
            null, 1598537346794,
            1598536519482, Transaction.TransactionStatus.SUCCESS, "1000000000000000000",
            "0x1bbbed2930395229b8f20fe1bad356b50a6b3f6f",
            "0x123c2124b7f2c18b502296ba884d9cde201f1c32",
            TransactionDetails("Appcoins Trivial Drive demo sample",
                TransactionDetails.Icon(TransactionDetails.Icon.Type.URL,
                    "https://cdn6.aptoide.com/imgs/5/1/d/51d9afee5beb29fd38c46d5eabcdefbe_icon.png"),
                "gas"), "APPC", Collections.emptyList())
    val iapBonusTransaction =
        Transaction("0xfec381f4943569add55b55cd75bef5308a1c843b28aa5b5fc75f3bd2be6dacc9",
            Transaction.TransactionType.BONUS, null, null, null, null,
            null, 1598536660586,
            1598536807269, Transaction.TransactionStatus.SUCCESS, "115000000000000000",
            "0x31a16adf2d5fc73f149fbb779d20c036678b1bbd",
            "0x1bbbed2930395229b8f20fe1bad356b50a6b3f6f",
            TransactionDetails("11.5", TransactionDetails.Icon(TransactionDetails.Icon.Type.URL,
                "https://apichain-dev.blockchainds.com/appc/icons/bonus.png"),
                "gas"), "APPC", Collections.emptyList())
    val iterator = transactions.iterator()
    for ((index, transaction) in iterator.withIndex()) {
      val testTransaction = when (index) {
        0 -> iapBonusTransaction
        1 -> iapTransaction
        2 -> perkBonusTransaction
        3 -> topUpBonusTransaction
        4 -> topUpTransaction
        else -> null
      }
      Assert.assertNotNull(testTransaction)
      Assert.assertEquals(transaction.transactionId, testTransaction!!.transactionId)
      Assert.assertEquals(transaction.type, testTransaction.type)
      Assert.assertEquals(transaction.subType, testTransaction.subType)
      Assert.assertEquals(transaction.title, testTransaction.title)
      Assert.assertEquals(transaction.description, testTransaction.description)
      Assert.assertEquals(transaction.perk, testTransaction.perk)
      Assert.assertEquals(transaction.approveTransactionId, testTransaction.approveTransactionId)
      Assert.assertNotNull(transaction.timeStamp)
      Assert.assertNotNull(transaction.processedTime)
      Assert.assertEquals(transaction.status, testTransaction.status)
      Assert.assertEquals(transaction.value, testTransaction.value)
      Assert.assertEquals(transaction.from, testTransaction.from)
      Assert.assertEquals(transaction.to, testTransaction.to)
      Assert.assertEquals(transaction.details!!.sourceName, testTransaction.details!!.sourceName)
      Assert.assertEquals(transaction.details!!.icon, testTransaction.details!!.icon)
      Assert.assertEquals(transaction.details!!.description, testTransaction.details!!.description)
      Assert.assertEquals(transaction.currency, testTransaction.currency)
      Assert.assertEquals(transaction.operations!!.size, testTransaction.operations!!.size)
    }
  }

  @Test
  @Throws(ParseException::class)
  fun dateFormatTest() {
    val receiveDateFormat: DateFormat =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
    val date = receiveDateFormat.parse("2019-09-17 11:34:21.563408+0000")
    println(receiveDateFormat.format(date!!))
  }
}