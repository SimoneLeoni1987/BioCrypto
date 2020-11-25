package com.example.biocrypto

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.example.biocrypto.biometrics.Biometrics
import com.example.biocrypto.crypto.CryptographyManager
import com.example.biocrypto.databinding.ActivityMainBinding
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

    enum class Operation {
        Encrypt, Decrypt
    }

    private lateinit var iv: ByteArray
    private val secretKeyName = "my_key"
    private lateinit var cipherText: ByteArray
    private var cryptographyManager = CryptographyManager()
    private var readyToEncrypt: Boolean = false

    private lateinit var etText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)

        binding.btnEncrypt.setOnClickListener {


            val cipher = cryptographyManager.getInitializedCipherForEncryption(secretKeyName)
            val liveData = Biometrics.authenticateToEncrypt(cipher, this)

            // try to extract the observer
            liveData.observe(this, Observer {
                Log.d(TAG, "From authentication")

                if (it != null) {
                    processData(it, Operation.Encrypt)
                }

            })

        }

        binding.btnDecrypt.setOnClickListener {
            //authenticateToDecrypt()
            val cipher = cryptographyManager.getInitializedCipherForDecryption(secretKeyName, iv)
            val liveData = Biometrics.authenticateToDecrypt(cipher, this)

            // try to extract the observer
            liveData.observe(this, Observer {
                Log.d(TAG, "From authentication")

                if (it != null) {
                    processData(it, Operation.Decrypt)
                }

            })

        }

        etText = binding.etText

        setContentView(binding.root)
    }



    private fun processData(cryptoObject: BiometricPrompt.CryptoObject?, operation: Operation) {

        // Decrypt encrypt
        Log.d(TAG, "Process data with crypto object")

        val data = when (operation) {
            Operation.Encrypt -> {
                // encrypt
                val text = etText.text.toString()
                val encrypted = cryptographyManager.encryptData(text, cryptoObject?.cipher!!)

                iv = encrypted.initializationVector

                cipherText = encrypted.ciphertext

                String(encrypted.ciphertext, Charset.forName("UTF-8"))
            }
            Operation.Decrypt ->  cryptographyManager.decryptData(cipherText, cryptoObject?.cipher!!)
        }


        etText.setText(data)

    }


    companion object {
        private const val TAG = "MainActivity"
    }
}