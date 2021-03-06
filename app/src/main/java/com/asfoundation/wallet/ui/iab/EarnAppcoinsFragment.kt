package com.asfoundation.wallet.ui.iab

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.dialog_buy_buttons_payment_methods.*
import kotlinx.android.synthetic.main.dialog_buy_buttons_payment_methods.view.*
import kotlinx.android.synthetic.main.earn_appcoins_layout.*
import java.math.BigDecimal
import javax.inject.Inject

class EarnAppcoinsFragment : BasePageViewFragment(), EarnAppcoinsView {

  private lateinit var presenter: EarnAppcoinsPresenter
  private lateinit var iabView: IabView

  @Inject
  lateinit var analytics: BillingAnalytics

  override fun onCreate(savedInstanceState: Bundle?) {
    if (savedInstanceState == null) {
      analytics.sendPaymentEvent(domain, skuId, amount.toString(),
          PAYMENT_METHOD_NAME, type)
    }
    presenter = EarnAppcoinsPresenter(this, CompositeDisposable(), AndroidSchedulers.mainThread())
    super.onCreate(savedInstanceState)
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(context is IabView) { "Earn Appcoins fragment must be attached to IAB activity" }
    iabView = context
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    dialog_buy_buttons_payment_methods.buy_button.text = getString(R.string.discover_button)
    dialog_buy_buttons_payment_methods.cancel_button.text = getString(R.string.back_button)
    iabView.disableBack()
    presenter.present()
    super.onViewCreated(view, savedInstanceState)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.earn_appcoins_layout, container, false)
  }

  override fun backButtonClick(): Observable<Any> {
    return RxView.clicks(cancel_button)
  }

  override fun discoverButtonClick(): Observable<Any> {
    return RxView.clicks(buy_button)
  }

  override fun navigateBack() {
    iabView.showPaymentMethodsView()
  }

  override fun backPressed() = iabView.backButtonPress()

  override fun navigateToAptoide() {
    val intent = Intent(Intent.ACTION_VIEW).apply {
      val packageManager = context?.packageManager
      this.data = Uri.parse(APTOIDE_EARN_APPCOINS_DEEP_LINK)
      val appsList =
          packageManager?.queryIntentActivities(this, PackageManager.MATCH_DEFAULT_ONLY)
      appsList?.first { it.activityInfo.packageName == "cm.aptoide.pt" }
          ?.let {
            setPackage(it.activityInfo.packageName)
          }
    }
    iabView.launchIntent(intent)
  }

  override fun onDestroyView() {
    iabView.enableBack()
    presenter.destroy()
    super.onDestroyView()
  }

  val domain: String by lazy {
    if (arguments!!.containsKey(PARAM_DOMAIN)) {
      arguments!!.getString(PARAM_DOMAIN, "")
    } else {
      throw IllegalArgumentException("Domain not found")
    }
  }

  val skuId: String? by lazy {
    if (arguments!!.containsKey(PARAM_SKUID)) {
      val value = arguments!!.getString(PARAM_SKUID) ?: return@lazy null
      value
    } else {
      throw IllegalArgumentException("SkuId not found")
    }
  }

  val amount: BigDecimal by lazy {
    if (arguments!!.containsKey(PARAM_AMOUNT)) {
      val value = arguments!!.getSerializable(PARAM_AMOUNT) as BigDecimal
      value
    } else {
      throw IllegalArgumentException("amount not found")
    }
  }

  val type: String by lazy {
    if (arguments!!.containsKey(PARAM_TRANSACTION_TYPE)) {
      arguments!!.getString(PARAM_TRANSACTION_TYPE, "")
    } else {
      throw IllegalArgumentException("type not found")
    }
  }

  companion object {

    @JvmStatic
    fun newInstance(domain: String, skuId: String?, amount: BigDecimal,
                    type: String): EarnAppcoinsFragment = EarnAppcoinsFragment().apply {
      arguments = Bundle().apply {
        putString(PARAM_DOMAIN, domain)
        putString(PARAM_SKUID, skuId)
        putString(PARAM_TRANSACTION_TYPE, type)
        putSerializable(PARAM_AMOUNT, amount)
      }
    }

    private const val APTOIDE_EARN_APPCOINS_DEEP_LINK =
        "aptoide://cm.aptoide.pt/deeplink?name=appcoins_ads"
    private const val PARAM_DOMAIN = "AMOUNT_DOMAIN"
    private const val PARAM_SKUID = "AMOUNT_SKUID"
    private const val PARAM_AMOUNT = "PARAM_AMOUNT"
    private const val PARAM_TRANSACTION_TYPE = "PARAM_TRANSACTION_TYPE"
    private const val PAYMENT_METHOD_NAME = "EARN_APPCOINS_BUNDLE"
  }
}
