package com.asfoundation.wallet.di

import android.content.Context
import cm.aptoide.analytics.AnalyticsManager
import com.asfoundation.wallet.advertise.PoaAnalyticsController
import com.asfoundation.wallet.analytics.*
import com.asfoundation.wallet.analytics.gamification.GamificationAnalytics
import com.asfoundation.wallet.billing.analytics.*
import com.asfoundation.wallet.identification.IdsRepository
import com.asfoundation.wallet.logging.Logger
import com.asfoundation.wallet.topup.TopUpAnalytics
import com.asfoundation.wallet.transactions.TransactionsAnalytics
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.iab.PaymentMethodsAnalytics
import com.asfoundation.wallet.ui.iab.local_payments.LocalPaymentAnalytics
import com.asfoundation.wallet.wallet_validation.generic.WalletValidationAnalytics
import com.facebook.appevents.AppEventsLogger
import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Named
import javax.inject.Singleton

@Module
class AnalyticsModule {

  @Provides
  fun provideLocalPaymentAnalytics(billingAnalytics: BillingAnalytics,
                                   inAppPurchaseInteractor: InAppPurchaseInteractor): LocalPaymentAnalytics {
    return LocalPaymentAnalytics(billingAnalytics, inAppPurchaseInteractor, Schedulers.io())
  }

  @Singleton
  @Provides
  fun providesPageViewAnalytics(analyticsManager: AnalyticsManager): PageViewAnalytics {
    return PageViewAnalytics(analyticsManager)
  }

  @Singleton
  @Provides
  @Named("bi_event_list")
  fun provideBiEventList() = listOf(
      BillingAnalytics.PURCHASE_DETAILS,
      BillingAnalytics.PAYMENT_METHOD_DETAILS,
      BillingAnalytics.PAYMENT,
      PoaAnalytics.POA_STARTED,
      PoaAnalytics.POA_COMPLETED)

  @Singleton
  @Provides
  @Named("facebook_event_list")
  fun provideFacebookEventList() = listOf(
      BillingAnalytics.PURCHASE_DETAILS,
      BillingAnalytics.PAYMENT_METHOD_DETAILS,
      BillingAnalytics.PAYMENT,
      BillingAnalytics.REVENUE,
      PoaAnalytics.POA_STARTED,
      PoaAnalytics.POA_COMPLETED,
      TransactionsAnalytics.OPEN_APPLICATION,
      GamificationAnalytics.GAMIFICATION,
      GamificationAnalytics.GAMIFICATION_MORE_INFO
  )

  @Singleton
  @Provides
  @Named("rakam_event_list")
  fun provideRakamEventList() = listOf(
      BillingAnalytics.RAKAM_PRESELECTED_PAYMENT_METHOD,
      BillingAnalytics.RAKAM_PAYMENT_METHOD,
      BillingAnalytics.RAKAM_PAYMENT_CONFIRMATION,
      BillingAnalytics.RAKAM_PAYMENT_CONCLUSION,
      BillingAnalytics.RAKAM_PAYMENT_START,
      BillingAnalytics.RAKAM_PAYPAL_URL,
      TopUpAnalytics.WALLET_TOP_UP_START,
      TopUpAnalytics.WALLET_TOP_UP_SELECTION,
      TopUpAnalytics.WALLET_TOP_UP_CONFIRMATION,
      TopUpAnalytics.WALLET_TOP_UP_CONCLUSION,
      TopUpAnalytics.WALLET_TOP_UP_PAYPAL_URL,
      PoaAnalytics.RAKAM_POA_EVENT,
      WalletValidationAnalytics.WALLET_PHONE_NUMBER_VERIFICATION,
      WalletValidationAnalytics.WALLET_CODE_VERIFICATION,
      WalletValidationAnalytics.WALLET_VERIFICATION_CONFIRMATION,
      WalletsAnalytics.WALLET_CREATE_BACKUP,
      WalletsAnalytics.WALLET_SAVE_BACKUP,
      WalletsAnalytics.WALLET_CONFIRMATION_BACKUP,
      WalletsAnalytics.WALLET_SAVE_FILE,
      WalletsAnalytics.WALLET_IMPORT_RESTORE,
      WalletsAnalytics.WALLET_PASSWORD_RESTORE,
      PageViewAnalytics.WALLET_PAGE_VIEW
  )

  @Singleton
  @Provides
  @Named("amplitude_event_list")
  fun provideAmplitudeEventList() = listOf(
      BillingAnalytics.RAKAM_PRESELECTED_PAYMENT_METHOD,
      BillingAnalytics.RAKAM_PAYMENT_METHOD,
      BillingAnalytics.RAKAM_PAYMENT_CONFIRMATION,
      BillingAnalytics.RAKAM_PAYMENT_CONCLUSION,
      BillingAnalytics.RAKAM_PAYMENT_START,
      BillingAnalytics.RAKAM_PAYPAL_URL,
      TopUpAnalytics.WALLET_TOP_UP_START,
      TopUpAnalytics.WALLET_TOP_UP_SELECTION,
      TopUpAnalytics.WALLET_TOP_UP_CONFIRMATION,
      TopUpAnalytics.WALLET_TOP_UP_CONCLUSION,
      TopUpAnalytics.WALLET_TOP_UP_PAYPAL_URL,
      PoaAnalytics.RAKAM_POA_EVENT,
      WalletValidationAnalytics.WALLET_PHONE_NUMBER_VERIFICATION,
      WalletValidationAnalytics.WALLET_CODE_VERIFICATION,
      WalletValidationAnalytics.WALLET_VERIFICATION_CONFIRMATION,
      WalletsAnalytics.WALLET_CREATE_BACKUP,
      WalletsAnalytics.WALLET_SAVE_BACKUP,
      WalletsAnalytics.WALLET_CONFIRMATION_BACKUP,
      WalletsAnalytics.WALLET_SAVE_FILE,
      WalletsAnalytics.WALLET_IMPORT_RESTORE,
      WalletsAnalytics.WALLET_PASSWORD_RESTORE,
      PageViewAnalytics.WALLET_PAGE_VIEW
  )

  @Singleton
  @Provides
  fun provideAnalyticsManager(@Named("default") okHttpClient: OkHttpClient, api: AnalyticsAPI,
                              context: Context, @Named("bi_event_list") biEventList: List<String>,
                              @Named("facebook_event_list") facebookEventList: List<String>,
                              @Named("rakam_event_list") rakamEventList: List<String>,
                              @Named("amplitude_event_list")
                              amplitudeEventList: List<String>): AnalyticsManager {
    return AnalyticsManager.Builder()
        .addLogger(BackendEventLogger(api), biEventList)
        .addLogger(FacebookEventLogger(AppEventsLogger.newLogger(context)), facebookEventList)
        .addLogger(RakamEventLogger(), rakamEventList)
        .addLogger(AmplitudeEventLogger(), amplitudeEventList)
        .setAnalyticsNormalizer(KeysNormalizer())
        .setDebugLogger(LogcatAnalyticsLogger())
        .setKnockLogger(HttpClientKnockLogger(okHttpClient))
        .build()
  }

  @Singleton
  @Provides
  fun provideWalletEventSender(analytics: AnalyticsManager): WalletsEventSender =
      WalletsAnalytics(analytics)

  @Singleton
  @Provides
  fun provideBillingAnalytics(analytics: AnalyticsManager) = BillingAnalytics(analytics)

  @Singleton
  @Provides
  fun providePoAAnalytics(analytics: AnalyticsManager) = PoaAnalytics(analytics)

  @Singleton
  @Provides
  fun providesPoaAnalyticsController() = PoaAnalyticsController(CopyOnWriteArrayList())

  @Singleton
  @Provides
  fun providesTransactionsAnalytics(analytics: AnalyticsManager) = TransactionsAnalytics(analytics)

  @Singleton
  @Provides
  fun provideGamificationAnalytics(analytics: AnalyticsManager) = GamificationAnalytics(analytics)

  @Singleton
  @Provides
  fun provideRakamAnalyticsSetup(context: Context, idsRepository: IdsRepository,
                                 logger: Logger): RakamAnalytics {
    return RakamAnalytics(context, idsRepository, logger)
  }

  @Singleton
  @Provides
  fun provideAmplitudeAnalytics(context: Context,
                                idsRepository: IdsRepository): AmplitudeAnalytics {
    return AmplitudeAnalytics(context, idsRepository)
  }

  @Singleton
  @Provides
  fun provideTopUpAnalytics(analyticsManager: AnalyticsManager) = TopUpAnalytics(analyticsManager)

  @Singleton
  @Provides
  fun provideWalletValidationAnalytics(analyticsManager: AnalyticsManager) =
      WalletValidationAnalytics(analyticsManager)

  @Provides
  fun providePaymentMethodsAnalytics(billingAnalytics: BillingAnalytics,
                                     rakamAnalytics: RakamAnalytics,
                                     amplitudeAnalytics: AmplitudeAnalytics): PaymentMethodsAnalytics {
    return PaymentMethodsAnalytics(billingAnalytics, rakamAnalytics, amplitudeAnalytics)
  }
}