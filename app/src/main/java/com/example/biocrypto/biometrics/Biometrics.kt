package com.example.biocrypto.biometrics

import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.biocrypto.R
import javax.crypto.Cipher

class Biometrics {

    companion object {

        private fun createPromptInfo(fragmentActivity: FragmentActivity): BiometricPrompt.PromptInfo {
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(fragmentActivity.getString(R.string.prompt_info_title))
                .setSubtitle(fragmentActivity.getString(R.string.prompt_info_subtitle))
                .setDescription(fragmentActivity.getString(R.string.prompt_info_description))
                .setConfirmationRequired(false)
                .setNegativeButtonText(fragmentActivity.getString(R.string.prompt_info_cancel))
                // .setDeviceCredentialAllowed(true) // Allow PIN/pattern/password authentication.
                // Also note that setDeviceCredentialAllowed and setNegativeButtonText are
                // incompatible so that if you uncomment one you must comment out the other
                .build()
            return promptInfo
        }

        private fun createBiometricPrompt(
            fragmentActivity: FragmentActivity,
            liveData: MutableLiveData<BiometricPrompt.CryptoObject>
        ): BiometricPrompt {
            val executor = ContextCompat.getMainExecutor(fragmentActivity)

            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.d(TAG, "$errorCode :: $errString")

                    liveData.postValue(null)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.d(TAG, "Authentication failed for an unknown reason")

                    liveData.postValue(null)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Log.d(TAG, "Authentication was successful")
                    //processData(result.cryptoObject)

                    liveData.postValue(result.cryptoObject)
                }
            }

            //The API requires the client/Activity context for displaying the prompt
            return BiometricPrompt(fragmentActivity, executor, callback)
        }


         fun authenticateToEncrypt(cipher: Cipher, fragmentActivity: FragmentActivity): LiveData<BiometricPrompt.CryptoObject> {
            //readyToEncrypt = true

             val liveData = MutableLiveData<BiometricPrompt.CryptoObject>()

             if (BiometricManager.from(fragmentActivity).canAuthenticate() == BiometricManager
                    .BIOMETRIC_SUCCESS

                     // Return false in some way with the livedata
            ) {
                createBiometricPrompt(fragmentActivity, liveData).authenticate(createPromptInfo(fragmentActivity), BiometricPrompt.CryptoObject(cipher))
            }

             return liveData
         }

         fun authenticateToDecrypt(cipher: Cipher, fragmentActivity: FragmentActivity): LiveData<BiometricPrompt.CryptoObject> {
            //readyToEncrypt = false
             val liveData = MutableLiveData<BiometricPrompt.CryptoObject>()

            if (BiometricManager.from(fragmentActivity).canAuthenticate() == BiometricManager
                    .BIOMETRIC_SUCCESS
            ) {
                createBiometricPrompt(fragmentActivity, liveData).authenticate(createPromptInfo(fragmentActivity), BiometricPrompt.CryptoObject(cipher))
            }

             return liveData
         }

        private const val TAG = "Biometrics"
    }
}