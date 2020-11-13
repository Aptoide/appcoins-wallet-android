package com.asfoundation.wallet.ui.backup

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.documentfile.provider.DocumentFile
import com.asf.wallet.R
import com.asfoundation.wallet.billing.analytics.WalletsAnalytics
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.permissions.manage.view.ToolbarManager
import com.asfoundation.wallet.ui.BaseActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_backup.*
import javax.inject.Inject

@AndroidEntryPoint
class WalletBackupActivity : BaseActivity(), BackupActivityView, ToolbarManager {

  companion object {
    @JvmStatic
    fun newIntent(context: Context, walletAddress: String) =
        Intent(context, WalletBackupActivity::class.java).apply {
          putExtra(WALLET_ADDRESS, walletAddress)
        }

    private const val WALLET_ADDRESS = "wallet_addr"
    private const val FILE_NAME_EXTRA_KEY = "file_name"
    private const val RC_WRITE_EXTERNAL_STORAGE_PERMISSION = 1000
    private const val ACTION_OPEN_DOCUMENT_TREE_REQUEST_CODE = 1001

  }

  @Inject
  lateinit var walletsEventSender: WalletsEventSender
  private lateinit var presenter: BackupActivityPresenter
  private var onPermissionSubject: PublishSubject<Unit>? = null
  private var onDocumentFileSubject: PublishSubject<SystemFileIntentResult>? = null

  private val walletAddress: String by lazy {
    if (intent.extras!!.containsKey(WALLET_ADDRESS)) {
      intent.extras!!.getString(WALLET_ADDRESS)!!
    } else {
      throw IllegalArgumentException("Wallet address not found")
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_backup)
    onPermissionSubject = PublishSubject.create()
    onDocumentFileSubject = PublishSubject.create()
    presenter = BackupActivityPresenter(this)
    presenter.present(savedInstanceState == null)
    savedInstanceState?.let { setupToolbar() }
  }

  override fun showBackupScreen() {
    presenter.currentFragmentName = BackupWalletFragment::class.java.simpleName
    setupToolbar()
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, BackupWalletFragment.newInstance(walletAddress))
        .commit()
  }

  override fun showBackupCreationScreen(password: String) {
    presenter.currentFragmentName = BackupCreationFragment::class.java.simpleName
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            BackupCreationFragment.newInstance(walletAddress, password))
        .commit()
  }

  override fun askForWritePermissions() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
        ActivityCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
      onPermissionSubject?.onNext(Unit)
    } else {
      requestStorageWritePermission()
    }
  }

  override fun showSuccessScreen() {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container, BackupSuccessFragment.newInstance())
        .commit()
  }

  override fun closeScreen() = finish()

  override fun onPermissionGiven() = onPermissionSubject!!

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      android.R.id.home -> {
        when (presenter.currentFragmentName) {
          BackupCreationFragment::class.java.simpleName -> walletsEventSender.sendWalletSaveFileEvent(
              WalletsAnalytics.ACTION_BACK, WalletsAnalytics.STATUS_FAIL,
              WalletsAnalytics.REASON_CANCELED)
        }
      }
    }
    return super.onOptionsItemSelected(item)
  }

  override fun openSystemFileDirectory(fileName: String) {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
      putExtra(FILE_NAME_EXTRA_KEY, fileName)
    }
    try {
      startActivityForResult(intent, ACTION_OPEN_DOCUMENT_TREE_REQUEST_CODE)
    } catch (ex: ActivityNotFoundException) {
      Snackbar.make(backup_content, R.string.unknown_error, Snackbar.LENGTH_SHORT)
          .show()
    }
  }

  private fun requestStorageWritePermission() =
      ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
          RC_WRITE_EXTERNAL_STORAGE_PERMISSION)

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                          grantResults: IntArray) {
    if (requestCode == RC_WRITE_EXTERNAL_STORAGE_PERMISSION) {
      if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        onPermissionSubject?.onNext(Unit)
      }
    } else {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == ACTION_OPEN_DOCUMENT_TREE_REQUEST_CODE) {
      var systemFileIntentResult = SystemFileIntentResult()
      if (resultCode == RESULT_OK && data != null) {
        data.data?.let {
          val documentFile = DocumentFile.fromTreeUri(this, it)
          systemFileIntentResult = SystemFileIntentResult(documentFile)
        }
      }
      onDocumentFileSubject?.onNext(systemFileIntentResult)
    }
  }

  override fun onSystemFileIntentResult() = onDocumentFileSubject!!

  override fun setupToolbar() {
    toolbar()
  }

  override fun onDestroy() {
    onPermissionSubject = null
    onDocumentFileSubject = null
    super.onDestroy()
  }
}