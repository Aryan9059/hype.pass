//package com.pass.hype
//
//import android.R
//import android.os.Build
//import android.os.CancellationSignal
//import android.service.autofill.AutofillService
//import android.service.autofill.Dataset
//import android.service.autofill.FillCallback
//import android.service.autofill.FillRequest
//import android.service.autofill.FillResponse
//import android.service.autofill.SaveCallback
//import android.service.autofill.SaveRequest
//import android.view.View
//import android.view.ViewStructure
//import android.view.autofill.AutofillId
//import android.view.autofill.AutofillValue
//import android.widget.RemoteViews
//import androidx.annotation.RequiresApi
//
//@RequiresApi(Build.VERSION_CODES.O)
//class AutofillService : AutofillService() {
//    override fun onFillRequest(request: FillRequest, cancellationSignal: CancellationSignal, callback: FillCallback) {
//
//        val database = HypePass.passwordDatabase
//        val passwordDao = database.passwordDao()
//
//        val fillContexts = request.fillContexts
//        val latestContext = fillContexts[fillContexts.size - 1] // Get the latest view context
//        val structure = latestContext.structure
//
//        val usernamePresentation = RemoteViews(packageName, R.layout.simple_list_item_1)
//        usernamePresentation.setTextViewText(R.id.text1, "Username")
//
//        val passwordPresentation = RemoteViews(packageName, R.layout.simple_list_item_1)
//        passwordPresentation.setTextViewText(R.id.text1, "Password")
//
//        var emailAutofillId: AutofillId? = null
//        var passwordAutofillId: AutofillId? = null
//
//        passwordDao.getAllPasswords().value?.forEach{ password ->
//            val fillResponse: FillResponse = FillResponse.Builder()
//                .addDataset(
//                    Dataset.Builder()
//                        .setValue(
//                            password.passwordId,
//                            AutofillValue.forText(password.email),
//                            usernamePresentation
//                        )
//                        .setValue(
//                            password.passwordId,
//                            AutofillValue.forText(password.password),
//                            passwordPresentation
//                        )
//                        .build()
//                )
//                .build()
//
//            callback.onSuccess(fillResponse)
//        }
//    }
//
//    override fun onSaveRequest(p0: SaveRequest, p1: SaveCallback) {
//        TODO("Not yet implemented")
//    }
//}