package com.mrayush.calculator

import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.graphics.Bitmap
import android.graphics.Path.Op
import android.graphics.drawable.BitmapDrawable
import android.icu.number.ScientificNotation
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.tasks.Task
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import java.io.File
import java.io.FileOutputStream
import kotlin.math.*


class MainActivity : AppCompatActivity() {

    private var operation: Operation = Operation.EMPTY
    private var firstProcessingNumber = 0.0
    private var secondProcessingNumber = 0.0
    private var is_errored_text = false
    private var is_ans_showed = false
    lateinit var drawerlayout : DrawerLayout
    lateinit var navigationView : NavigationView
    lateinit var toolbar : Toolbar
    private var btnShowDialog: Button? = null
    private var reviewInfo : ReviewInfo? = null
    private var manager : ReviewManager? = null
    private val TAG = "Update_Button"
    private var UPDATE_REQUEST_CODE = 100
    private lateinit var appUpdateManager : AppUpdateManager
    private var player : MediaPlayer?=null
    private var darkmode = false

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("darkmode", darkmode)
    }

  /* if (savedInstanceState != null) {
            darkmode = savedInstanceState.getBoolean("darkmode")
        }
*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        appUpdateManager = AppUpdateManagerFactory.create(this)

        initListeners()
        activateReviewInfo()
        update(false)

        drawerlayout = findViewById(R.id.menu_drawer)
        navigationView = findViewById(R.id.navigation_menu)
        toolbar = findViewById(R.id.menuButton)
        setSupportActionBar(toolbar)

        val toggle : ActionBarDrawerToggle = ActionBarDrawerToggle(this, drawerlayout, toolbar, R.string.navigation_open, R.string.navigation_close)

        drawerlayout.addDrawerListener(toggle)
        toggle.isDrawerIndicatorEnabled = true
        toggle.syncState()
        navigationView = findViewById<View>(R.id.navigation_menu) as NavigationView
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
           //     R.id.preferences -> darkmode()
                R.id.developer -> portFolioIntent()
                R.id.share ->  share()
                R.id.feedback -> startReviewFlow()
                R.id.update -> update(true)
                R.id.source_code -> showDialog()
                R.id.about_us -> aboutUsIntent()
                R.id.appVersionOption -> AppVersionOption()
            }
            closeDrawer()
            false
        }

        daynight()
    }

    private fun AppVersionOption(){
        vibration()
        Toast.makeText(this@MainActivity,getString(R.string.About_version) + getString(R.string.appVersion),Toast.LENGTH_LONG).show()
    }


    private fun daynight(){
        vibration()
        val swtch: Switch
        swtch = findViewById(R.id.daynight)
        swtch.setOnCheckedChangeListener { compoundButton, b ->
            if(swtch.isChecked) {
                getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                Toast.makeText(this@MainActivity, getString(R.string.Night_Mode_on), Toast.LENGTH_SHORT).show()

            }
            else {
                getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                Toast.makeText(this@MainActivity, getString(R.string.Night_Mode_off), Toast.LENGTH_SHORT).show()
            }
        }

    }
   /* private fun darkmode() {
        vibration()
        val share: Button
        val swtch: Switch
        val close: ImageButton
        val dialog = Dialog(this@MainActivity)
        dialog.setContentView(R.layout.darkmodeprompt)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        val window = dialog.window
        window!!.setGravity(Gravity.CENTER)
        window.attributes.windowAnimations = R.style.DialogAnimation
        swtch = dialog.findViewById(R.id.modebtn)
        close = dialog.findViewById(R.id.closePopup)
        swtch.isChecked = darkmode
        swtch.setOnCheckedChangeListener { compoundButton, b ->
            darkmode = !darkmode
            if(darkmode) {
                getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                Toast.makeText(this@MainActivity, "Its on", Toast.LENGTH_SHORT).show()
            }
            else {
                getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                Toast.makeText(this@MainActivity, "Its off", Toast.LENGTH_SHORT).show()
            }
        }

        close.setOnClickListener {
            vibration()
            dialog.dismiss()
        }
        dialog.setCancelable(true)
        window.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT)
        dialog.show()

    }
*/
    private fun aboutUsIntent(){
        onClickSound()
        vibration()
        val aboutUsIntent = Intent(this@MainActivity, AboutUs::class.java)
        startActivity(aboutUsIntent)
    }

    private fun portFolioIntent(){
        onClickSound()
        vibration()
        val portFolioIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://mrayush.me/?refer=calculator-"+getString(R.string.appVersion)))
        startActivity(portFolioIntent)
    }

    private fun share() {
        val i : ImageView = ImageView(applicationContext)
        i.setImageResource(R.drawable.banner)
        val bitmapDrawable = i.drawable as BitmapDrawable
        val bitmap = bitmapDrawable.bitmap
        val uri: Uri = getImageToShare(bitmap)
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.setType("image/*")
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject))
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text, getPackageName()))
        shareIntent.putExtra(Intent.EXTRA_STREAM,uri)
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_title)))
    }
    private fun getImageToShare(bitmap: Bitmap): Uri {
        val folder : File = File(cacheDir,"images")

        folder.mkdirs()
        val file: File = File(folder,"shared_image.jpg")
        val fileOutputStream: FileOutputStream = FileOutputStream(file)

        bitmap.compress(Bitmap.CompressFormat.JPEG,100,fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()

        val uri: Uri = FileProvider.getUriForFile(applicationContext,"com.mrayush.calculator",file)
        return uri

    }

    private fun showDialog() {
        onClickSound()
        vibration()
        val sourceCode: Button
        val close: ImageButton
        val dialog = Dialog(this@MainActivity)
        dialog.setContentView(R.layout.popup)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        val window = dialog.window
        window!!.setGravity(Gravity.CENTER)
        window.attributes.windowAnimations = R.style.DialogAnimation
        sourceCode = dialog.findViewById(R.id.popupSourceBtn)
        close = dialog.findViewById(R.id.closePopup)
        sourceCode.setOnClickListener {
            onClickSound()
            vibration()
            val URL : String = "https://github.com/AyushAgnihotri2025/Calculator"
            val browserIntent = Intent(Intent.ACTION_VIEW)
            browserIntent.data = Uri.parse(URL)
            startActivity(browserIntent)
        }
        close.setOnClickListener {
            onClickSound()
            vibration()
            dialog.dismiss()
        }
        dialog.setCancelable(true)
        window.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT)
        dialog.show()
    }

    private fun closeDrawer() {
        val drawer = findViewById<DrawerLayout>(R.id.menu_drawer)
        drawer.closeDrawer(GravityCompat.START)
    }


    private fun openPlayStore() {
        val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id="+getPackageName()))
        startActivity(playStoreIntent)
    }

    private fun activateReviewInfo() {
        manager = ReviewManagerFactory.create(this)
        val managerInfoTask: Task<ReviewInfo> = manager!!.requestReviewFlow()
        managerInfoTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                reviewInfo = task.result
            } else {
                Toast.makeText(this, getString(R.string.Error_In_Rating), Toast.LENGTH_SHORT).show()
                openPlayStore()
            }
        }
    }

    private fun startReviewFlow() {
        onClickSound()
        vibration()
        if (reviewInfo != null) {
            val flow: Task<Void> = manager!!.launchReviewFlow(this, reviewInfo!!)
            flow.addOnCompleteListener { task ->
                Toast.makeText(
                    this,
                    getString(R.string.Rating_successfull_message),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun update(is_force_update: Boolean) {
        if (is_force_update) {
            onClickSound()
            vibration()
            Toast.makeText(
                this,
                getString(R.string.Update_Check),
                Toast.LENGTH_SHORT
            ).show()
        }

        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        val listener = { state: InstallState  ->
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackBarForCompleteUpdate()
            }
        }

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
            Log.e(TAG, appUpdateInfo.updateAvailability().toString())
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                try {
                    appUpdateManager.registerListener(listener)
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.FLEXIBLE,
                        this@MainActivity,
                        UPDATE_REQUEST_CODE
                    )
                } catch(exception: IntentSender.SendIntentException) {
                    appUpdateManager.unregisterListener(listener)
                    Log.e(TAG, "callInUpdate : "+exception.message)
                }
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                if (is_force_update) {
                    Toast.makeText(
                        this,
                        getString(R.string.Open_Play_store),
                        Toast.LENGTH_SHORT
                    ).show()
                    openPlayStore()
                }
            } else {
                if (is_force_update) {
                    Toast.makeText(
                        this,
                        getString(R.string.Already_update),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun popupSnackBarForCompleteUpdate() {
        Snackbar.make(
            findViewById(R.id.context_view),
            getString(R.string.Update_downloading),
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction("RESTART") { appUpdateManager.completeUpdate() }
            setActionTextColor(resources.getColor(R.color.black))
            show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) return
        if (requestCode == UPDATE_REQUEST_CODE) {
            Log.e("MY_APP", "Update flow failed! Result code: $resultCode")
            if (resultCode == RESULT_OK) {
                Toast.makeText(
                    this,
                    "Updating the app.",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (resultCode != RESULT_OK) {
                if (resultCode == ActivityResult.RESULT_IN_APP_UPDATE_FAILED) {
                    Toast.makeText(
                        this,
                        getString(R.string.Open_Play_store) ,
                        Toast.LENGTH_SHORT
                    ).show()
                    openPlayStore()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.Update_completed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }



    private fun equalsButtonOnclick() {
        try {

            if(operation == Operation.log || operation == Operation.sqrt || operation == Operation.sin || operation == Operation.cos
                || operation == Operation.tan || operation == Operation.ln || operation == Operation.e)
            {
//                firstProcessingNumber =
//                    calculatorDisplayNonMock.text.toString().replace(',', '.').toDouble()
                val num = extractDouble(calculatorDisplayNonMock.text.toString()).toString()
                firstProcessingNumber = num.toDouble()

                val ans =
                    if ((floor(calculateExpression()) == ceil(calculateExpression())))
                        calculateExpression()
                            .toString().replace(".0", "")
                    else
                        calculateExpression().toString()
                // val rnd = ans.toInt()
                // val ans2 = (round(rnd.toDouble() * 1000.0)/1000.0).toString()
                calculatorDisplayNonMock.text = ans
            }
            if(operation == Operation.FACT)
            {
                val ans =
                    if ((floor(calculateExpression()) == ceil(calculateExpression())))
                        calculateExpression()
                            .toString().replace(".0", "")
                    else
                        calculateExpression().toString()
                // val rnd = ans.toInt()
                // val ans2 = (round(rnd.toDouble() * 1000.0)/1000.0).toString()
                calculatorDisplayNonMock.text = ans
            }
//            Log.d("MM", calculatorDisplayNonMock.text.toString().toBigDecimal().toString())
//            secondProcessingNumber =
//                calculatorDisplayNonMock.text.toString().replace(',', '.').toDouble()




            if (operation == Operation.DVD || operation == Operation.MUL || operation == Operation.POW || operation == Operation.PLUS
                || operation == Operation.MINUS || operation == Operation.nCr || operation == Operation.PERCENT)
            {

                val num2 = extractFirstNumberAfterOperator(calculatorDisplayNonMock.text.toString() , operation.toString()).toString()
                secondProcessingNumber = num2.toDouble()

                if (secondProcessingNumber == 0.0 && operation == Operation.DVD) {
                    Log.d("MM", calculatorDisplayNonMock.text.toString().toBigDecimal().toString())

                    val alertBuilder = AlertDialog.Builder(this)
                    alertBuilder.setTitle("Math Error")
                        .setMessage("Can't divide by zero")
                        .setCancelable(true)
                        .setPositiveButton("Ok"){dialogInterface, it ->
                            dialogInterface.cancel()
                        }
                        .show()
                    clearDisplay()
                }
                else {
                    val ans =
                        if ((floor(calculateExpression()) == ceil(calculateExpression())))
                            calculateExpression()
                                .toString().replace(".0", "")
                        else
                            calculateExpression().toString()
                    // val rnd = ans.toInt()
                    // val ans2 = (round(rnd.toDouble() * 1000.0)/1000.0).toString()
                    calculatorDisplayNonMock.text = ans
                }
            }

            else{
                val ans =
                    if ((floor(calculateExpression()) == ceil(calculateExpression())))
                        calculateExpression()
                            .toString().replace(".0", "")
                    else
                        calculateExpression().toString()
//                 val rnd = ans.toInt()
//                 val ans2 = (round(rnd.toDouble() * 1000.0)/1000.0).toString()
//                calculatorDisplayNonMock.text =
//                    if (ans.length > 9)
//                        "OVERFLOW"
//                    else
//                        ans
                if (ans.length > 9){
//                    var alertBuilder = AlertDialog.Builder(this)
//                    is_errored_text = true
//                    clearDisplay(true)
//                    alertBuilder.setTitle("Answer Overflow")
//                        .setMessage("Answer is : "+ans)
//                        .setCancelable(true)
//                        .setPositiveButton("Ok"){dialogInterface, it ->
//                            dialogInterface.cancel()
//                        }
//                        .show()
                    val number : Double = ans.toDouble()
                    val toast : Toast = Toast.makeText(this , "Full answer = "+ans , Toast.LENGTH_LONG)
                    toast.show()
                    val scientificNotation = String.format("%.2e", number)
                    calculatorDisplayNonMock.text = scientificNotation

                } else {
                    firstProcessingNumber = calculateExpression()
                    secondProcessingNumber = 0.0
                }

            }

        } catch (e: NumberFormatException) {
            calculatorDisplayNonMock.text = "ERROR"
            is_errored_text = true
            clearDisplay(true)
        }
        is_ans_showed = true
    }

    fun extractDouble(input: String): Double? {
        val regex = Regex("-?\\d+\\.?\\d*") // regular expression for matching numerical values
        val matchResult = regex.find(input)
        return matchResult?.value?.toDoubleOrNull()
    }

    fun extractFirstNumberAfterOperator(str: String, searchStr: String): Double? {
        val escapedSearchStr = Regex.escape(searchStr) // escape search string to avoid regex syntax errors
        val regex = Regex("$escapedSearchStr.*?(\\d+\\.?\\d*)") // regular expression for matching a numerical value after a search string
        val matchResult = regex.find(str)
        return matchResult?.groupValues?.getOrNull(1)?.toDoubleOrNull()
    }

    private fun clearDisplay(
        screen: Boolean = false,
        first_val: Boolean = true,
        second_val: Boolean = true,
        operator: Boolean = true
    )
    {
        if (!screen) {
            calculatorDisplayNonMock.text = ""
        }
        if (operator)
            operation = Operation.EMPTY
        if (first_val)
            firstProcessingNumber = 0.0
        if (second_val)
            secondProcessingNumber = 0.0
    }


    private fun calculateExpression(): Double {
        return when (operation) {
            Operation.DVD -> (firstProcessingNumber / secondProcessingNumber * 100000000).roundToLong()
                .toDouble() / 100000000
            Operation.MUL -> (firstProcessingNumber * secondProcessingNumber * 100000000).roundToLong()
                .toDouble() / 100000000
            Operation.MINUS -> ((firstProcessingNumber - secondProcessingNumber) * 100000000).roundToLong()
                .toDouble() / 100000000
            Operation.PLUS -> ((firstProcessingNumber + secondProcessingNumber) * 100000000).roundToLong()
                .toDouble() / 100000000
            Operation.PERCENT -> (firstProcessingNumber / 100 * secondProcessingNumber * 100000000).roundToLong()
                .toDouble() / 100000000
            Operation.POW -> (firstProcessingNumber.pow(secondProcessingNumber) * 100000000).roundToLong()
                .toDouble() / 100000000
            Operation.nCr -> (factorial(firstProcessingNumber)/(factorial(secondProcessingNumber)*factorial(firstProcessingNumber-secondProcessingNumber)) * 100000000).roundToLong() .toDouble() / 100000000
            Operation.FACT -> (factorial(firstProcessingNumber) * 100000000).roundToLong()
                .toDouble() / 100000000
            Operation.log -> (log10(firstProcessingNumber) * 100000000).roundToLong()
                .toDouble() / 100000000
            Operation.sqrt -> (sqrt(firstProcessingNumber) *100000000).roundToLong()
                .toDouble() / 100000000
            Operation.ln -> (log(firstProcessingNumber,2.7182818284) * 100000000).roundToLong()
                .toDouble() / 100000000
            Operation.sin -> (Math.round((sin(Math.toRadians(firstProcessingNumber)))*100000000).toDouble()/100000000)
            Operation.cos -> (Math.round((cos(Math.toRadians(firstProcessingNumber)))*100000000).toDouble()/100000000)
            Operation.tan -> (Math.round((tan(Math.toRadians(firstProcessingNumber)))*100000000).toDouble()/100000000)
            Operation.e-> (2.7182818284.pow(firstProcessingNumber)* 100000000).roundToLong()
                .toDouble() / 100000000
            else -> firstProcessingNumber
        }
    }

    private fun isAvailableToOperate(operation: Operation) {
        if (calculatorDisplayNonMock.text.toString()
                .isNotEmpty() && calculatorDisplayNonMock.text.toString() != "-"
        ) {
            onClickOperation(operation)
        }
        else
        {
            onClickOperation(operation)
        }

    }

    private fun checkOutputScreen(
        screen: Boolean = false,
        first_val: Boolean = true,
        second_val: Boolean = true,
        operator: Boolean = true,
        check_ans: Boolean = true

    ) {
        if (check_ans) {
            if (is_errored_text || is_ans_showed) {
                clearDisplay(
                    screen = screen,
                    first_val = first_val,
                    second_val = second_val,
                    operator = operator
                )
                is_errored_text = false
                is_ans_showed = false
            }
        } else {
            if (is_errored_text) {
                clearDisplay(
                    screen = screen,
                    first_val = first_val,
                    second_val = second_val,
                    operator = operator
                )
                is_errored_text = false
                is_ans_showed = false
            }
        }
    }

    private fun onClickOperation(processingOperation: Operation) {
        try {
            if (operation == Operation.EMPTY) {
                if (calculatorDisplayNonMock.text.toString().isNotEmpty()) {
                    firstProcessingNumber =
                        calculatorDisplayNonMock.text.toString().replace(',', '.').toDouble()
                    operation = processingOperation


                    calculatorDisplayNonMock.setText(firstProcessingNumber.toString() + operation.toString())
                }
                else{
                    operation = processingOperation
                    calculatorDisplayNonMock.setText(operation.toString())
                }

            }
        } catch (e: Exception){
            calculatorDisplayNonMock.text = "ERROR"
            is_errored_text = true
            clearDisplay(true)
        }
    }


     fun factorial(n: Double): Double {
         if (n < 0) {
             val errorMessage = "ERROR"
             Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
             return 0.0
         } else {
             return if (n == 1.00 || n == 0.00) 1.00 else n * factorial(n - 1)
         }

     }

    @SuppressLint("SetTextI18n")
    private fun initListeners() {
        val group = groupOfNumbers
        val refIds = group.referencedIds
        for (id in refIds) {
            findViewById<View>(id).setOnClickListener {
                vibration()
                checkOutputScreen(first_val=false, operator = false)
                calculatorDisplayNonMock.text =
                    "${calculatorDisplayNonMock.text.toString()}${(it as? Button)?.text.toString()}"
            }
        }

        clearDisplay()


        logButton.setOnClickListener{
            vibration()
            onClickSound()
            checkOutputScreen(second_val = false, check_ans=false)
            isAvailableToOperate(Operation.log)

        }

        expButton.setOnClickListener{
            vibration()
            onClickSound()
            checkOutputScreen(second_val = false, check_ans=false)
            isAvailableToOperate(Operation.e)

        }

        lnButton.setOnClickListener{
            vibration()
            onClickSound()
            checkOutputScreen(second_val = false, check_ans=false)
            isAvailableToOperate(Operation.ln)

        }

        factorialButton.setOnClickListener {
            onClickSound()
            vibration()
            checkOutputScreen(first_val = false,second_val = false, check_ans=false)
            isAvailableToOperate(Operation.FACT)
        }
        sqrt.setOnClickListener {
            onClickSound()
            vibration()
            checkOutputScreen(first_val = false,second_val = false, check_ans=false)
            isAvailableToOperate(Operation.sqrt)

        }
        sinButton.setOnClickListener {
            onClickSound()
            vibration()
            checkOutputScreen(first_val = false,second_val = false, check_ans=false)
            isAvailableToOperate(Operation.sin)
        }
        cosButton.setOnClickListener {
            onClickSound()
            vibration()
            checkOutputScreen(first_val = false,second_val = false, check_ans=false)
            isAvailableToOperate(Operation.cos)
        }
        tanButton.setOnClickListener {
            onClickSound()
            vibration()
            checkOutputScreen(first_val = false,second_val = false, check_ans=false)
            isAvailableToOperate(Operation.tan)
        }


        acButton.setOnClickListener {
            onClickSound()
            vibration()
            clearDisplay()
        }



        permutationButton.setOnClickListener {
            onClickSound()
            vibration()
            checkOutputScreen(first_val=false, check_ans=false)
            isAvailableToOperate(Operation.nCr)
        }

        powerButton.setOnClickListener {
            vibration()
            onClickSound()
            checkOutputScreen(first_val=false, check_ans=false)
            isAvailableToOperate(Operation.POW)
        }

        commaButton.setOnClickListener {
            vibration()
            checkOutputScreen()
            if (calculatorDisplayNonMock.text.toString()
                    .lastIndexOf(".") != calculatorDisplayNonMock.text.toString().length - 1
            )
                calculatorDisplayNonMock.text =
                    "${calculatorDisplayNonMock.text.toString()}."
        }

        divideButton.setOnClickListener {
            vibration()
            onClickSound()
            checkOutputScreen(first_val=false, check_ans=false)
            isAvailableToOperate(Operation.DVD)
        }

        multiplyButton.setOnClickListener {
            vibration()
            onClickSound()
            checkOutputScreen(first_val=false, check_ans=false)
            isAvailableToOperate(Operation.MUL)
        }

        minusButton.setOnClickListener {
            vibration()
            onClickSound()
            checkOutputScreen(first_val = false,second_val = false, check_ans=false)
            val displayAsString = calculatorDisplayNonMock.text.toString()
            try {
                if (displayAsString.isNotEmpty()) {
                    onClickOperation(Operation.MINUS)
                } else if (displayAsString.isEmpty() && displayAsString != "-"
                ) {
                    calculatorDisplayNonMock.text =
                        "${calculatorDisplayNonMock.text.toString()}-"
                }
            } catch (e: java.lang.NumberFormatException) {
                clearDisplay()
            }
        }



        plusButton.setOnClickListener {
            vibration()
            onClickSound()
            checkOutputScreen(first_val=false, check_ans=false)
            isAvailableToOperate(Operation.PLUS)

        }

        percentButton.setOnClickListener {
            vibration()
            onClickSound()
            checkOutputScreen(first_val=false, check_ans=false)
            isAvailableToOperate(Operation.PERCENT)

        }

        plusAndMinusButton.setOnClickListener {
            vibration()
            onClickSound()
            checkOutputScreen(screen = false, first_val = false, check_ans=false)
            if (calculatorDisplayNonMock.text.toString()
                    .isNotEmpty() && calculatorDisplayNonMock.text.toString() != "-"
            ) {
                firstProcessingNumber =
                    +calculatorDisplayNonMock.text.toString().replace(',', '.').toDouble() * -1
                calculatorDisplayNonMock.text =
                    if ((floor(firstProcessingNumber) == ceil(firstProcessingNumber)))
                        firstProcessingNumber
                            .toString().replace(".0", "")
                    else
                        firstProcessingNumber.toString()
            }
        }

        equalsButton.setOnClickListener {
            onClickSound()
            vibration()
            equalsButtonOnclick()
        }

        backspace.setOnClickListener {
            onClickSound()
            vibration()
            var value : String = calculatorDisplayNonMock.text.toString()
            if (value.length > 0){
                value = value.substring(0,value.length-1)
                calculatorDisplayNonMock.setText(value)
            }
        }
        piButton.setOnClickListener {
            onClickSound()
            vibration()
            if(operation != Operation.EMPTY)
                calculatorDisplayNonMock.setText(operation.toString() +"3.14")
            else
                calculatorDisplayNonMock.setText("3.14")

        }

    }

    private fun vibration(){
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) { // Vibrator availability checking
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE)) // New vibrate method for API Level 26 or higher
            } else {
                vibrator.vibrate(120) // Vibrate method for below API Level 26
            }
        }
    }


    private fun onClickSound(){
        try{
            val soundURI = Uri.parse(
                "android.resource://com.mrayush.calculator/"+R.raw.on_click_sound)
            player = MediaPlayer.create(applicationContext,soundURI)
            player?.isLooping =false
            player?.start()
        }catch (e: java.lang.Exception){
            e.printStackTrace()
        }
    }
}


