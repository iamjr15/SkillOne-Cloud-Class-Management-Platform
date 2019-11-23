package com.autohub.tutormodule.ui.dashboard.profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.autohub.skln.BaseActivity
import com.autohub.skln.models.User
import com.autohub.skln.models.batchRequests.GradeData
import com.autohub.skln.models.batchRequests.SubjectData
import com.autohub.skln.models.tutor.TutorData
import com.autohub.skln.models.tutor.TutorGradesSubjects
import com.autohub.skln.utills.AppConstants
import com.autohub.skln.utills.CommonUtils
import com.autohub.skln.utills.GlideApp
import com.autohub.tutormodule.R
import com.autohub.tutormodule.databinding.ActivityTutorEditProfileBinding
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import gun0912.tedbottompicker.TedBottomPicker
import kotlinx.android.synthetic.main.activity_tutor_edit_profile.*
import java.io.*
import java.util.*
import kotlin.collections.ArrayList

class EditProfileActivity : BaseActivity() {

    private lateinit var mBinding: ActivityTutorEditProfileBinding
    private val MAX_SIZE = 240
    private var mStorageReference: StorageReference? = null
    private var tutorData: TutorData? = null

    private var selectedSubjectsList = ArrayList<String>()
    private var subjectsList = ArrayList<SubjectData>()

    private var selectedGradesList = ArrayList<String>()
    private var gradesList = ArrayList<GradeData>()
    private var profilePictureUri: String = ""
    private var PermissionsRequest: Int = 12

    private val mWatcherWrapper = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            val remaining = MAX_SIZE - s?.length!!
            mBinding.count.text = String.format(Locale.getDefault(), "%d remaining", remaining)
            // editBio(s?.toString())
        }

    }

    private val selectedClass = ArrayList<String>()
    private val selectedExp = ArrayList<String>()
    private val selectedQualification = ArrayList<String>()
    private val selectedQualificationAreas = ArrayList<String>()
    private val selectedTargetBoard = ArrayList<String>()
    private val selectedSub = ArrayList<String>()
    private val selectedOccupation = ArrayList<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_tutor_edit_profile)
        mBinding.callback = this
        mStorageReference = FirebaseStorage.getInstance().reference
        mBinding.bio.addTextChangedListener(mWatcherWrapper)

        if (intent.hasExtra(getString(R.string.containsTutorData))) {
            setUpTutorInfo()
        }
    }


    private fun setUpTutorInfo() {
        showLoading()

        val tutor = intent.getParcelableExtra<TutorData>(getString(R.string.containsTutorData))

        this.tutorData = tutor
        mBinding.selectOccupation.text = tutor?.qualification?.currentOccupation
        mBinding.teachingExperience.text = tutor?.qualification?.experience
        mBinding.qualification.text = tutor?.qualification?.qualification
        mBinding.areaOfQualification.text = tutor?.qualification?.qualificationArea
        mBinding.targetedBoard.text = tutor?.qualification?.targetBoard
        mBinding.bio.setText(tutor?.personInfo?.biodata)


        GlideApp.with(this)
                .load(tutor?.personInfo?.accountPicture)
                .placeholder(com.autohub.skln.R.drawable.default_pic)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(mBinding.profilePicture)

        getTutorSubjects()
        getTutorGrades()

    }

    private fun getTutorSubjects() {
        firebaseStore.collection(getString(R.string.db_root_tutor_subjects)).whereEqualTo("teacherId", tutorData?.id).get()
                .addOnSuccessListener { documentSnapshot ->
                    val tutorSubjects = documentSnapshot.toObjects(TutorGradesSubjects::class.java)
                    getTutorSubjectsToTeach(tutorSubjects)
                }
                .addOnFailureListener { e ->
                    hideLoading()
                    showSnackError(e.message)
                }
    }

    private fun getTutorSubjectsToTeach(tutorSubjects: List<TutorGradesSubjects>) {
        firebaseStore.collection(getString(R.string.db_root_subjects)).get()
                .addOnSuccessListener { documentSnapshot ->
                    hideLoading()
                    val subjects = documentSnapshot.toObjects(SubjectData::class.java)
                    subjectsList = subjects as ArrayList<SubjectData>
                    for (i in tutorSubjects.indices) {
                        for (j in 0 until subjectsList.size) {
                            if (subjectsList[j].id.equals(tutorSubjects[i].subjectId)) {
                                selectedSubjectsList.add(subjectsList[j].name!!)
                            }
                        }
                    }
                    mBinding.subjectToTaught.text = selectedSubjectsList.joinToString(",")

                }
                .addOnFailureListener { e ->
                    hideLoading()
                    showSnackError(e.message)
                }


    }


    private fun getTutorGrades() {
        firebaseStore.collection(getString(R.string.db_root_tutor_gardes)).whereEqualTo("teacherId", tutorData?.id).get()
                .addOnSuccessListener { documentSnapshot ->
                    hideLoading()
                    val tutorGrades = documentSnapshot.toObjects(TutorGradesSubjects::class.java)
                    getTutorGradesToTeach(tutorGrades)
                }
                .addOnFailureListener { e ->
                    hideLoading()
                    showSnackError(e.message)
                }
    }

    private fun getTutorGradesToTeach(tutorGrades: List<TutorGradesSubjects>) {
        firebaseStore.collection(getString(R.string.db_root_grades)).get()
                .addOnSuccessListener { documentSnapshot ->
                    hideLoading()
                    val grades = documentSnapshot.toObjects(GradeData::class.java)
                    gradesList = grades as ArrayList<GradeData>
                    for (i in tutorGrades.indices) {
                        for (j in 0 until gradesList.size) {
                            if (gradesList[j].id.equals(tutorGrades[i].gradeId)) {
                                selectedGradesList.add(gradesList[j].name!!)
                            }
                        }
                    }
                    mBinding.classToTeach.text = selectedGradesList.joinToString(", ")
                }
                .addOnFailureListener { e ->
                    hideLoading()
                    showSnackError(e.message)
                }


    }

    fun onSubjectTaughtClick() {
        val items = ArrayList<String>()
        items.add(AppConstants.SUBJECT_SCIENCE)
        items.add(AppConstants.SUBJECT_COMPUTER_SCIENCE)
        items.add(AppConstants.SUBJECT_ACCOUNTANCY)
        items.add(AppConstants.SUBJECT_BIOLOGY)
        items.add(AppConstants.SUBJECT_BUSINESS)
        items.add(AppConstants.SUBJECT_SOCIAL_STUDIES)
        items.add(AppConstants.SUBJECT_CHEMISTRY)
        items.add(AppConstants.SUBJECT_ECONOMICS)
        items.add(AppConstants.SUBJECT_LANGUAGES)
        items.add(AppConstants.SUBJECT_PHYSICS)
        items.add(AppConstants.SUBJECT_MATHS)
        items.add(AppConstants.SUBJECT_ENGLISH)

        showMultiSelectionDialog(items, mBinding.subjectToTaught, getString(R.string.subject_to_taught), selectedSub)


    }

    fun onClassToTeach() {
//        showClassesToTeach()

        val items = ArrayList<String>()
        items.add("Class " + AppConstants.CLASS_1 + CommonUtils.getClassSuffix(AppConstants.CLASS_1.toInt()))
        items.add("Class " + AppConstants.CLASS_2 + CommonUtils.getClassSuffix(AppConstants.CLASS_2.toInt()))
        items.add("Class " + AppConstants.CLASS_3 + CommonUtils.getClassSuffix(AppConstants.CLASS_3.toInt()))
        items.add("Class " + AppConstants.CLASS_4 + CommonUtils.getClassSuffix(AppConstants.CLASS_4.toInt()))
        items.add("Class " + AppConstants.CLASS_5 + CommonUtils.getClassSuffix(AppConstants.CLASS_5.toInt()))
        items.add("Class " + AppConstants.CLASS_6 + CommonUtils.getClassSuffix(AppConstants.CLASS_6.toInt()))
        items.add("Class " + AppConstants.CLASS_7 + CommonUtils.getClassSuffix(AppConstants.CLASS_7.toInt()))
        items.add("Class " + AppConstants.CLASS_8 + CommonUtils.getClassSuffix(AppConstants.CLASS_8.toInt()))
        items.add("Class " + AppConstants.CLASS_9 + CommonUtils.getClassSuffix(AppConstants.CLASS_9.toInt()))
        items.add("Class " + AppConstants.CLASS_10 + CommonUtils.getClassSuffix(AppConstants.CLASS_10.toInt()))
        items.add("Class " + AppConstants.CLASS_11 + CommonUtils.getClassSuffix(AppConstants.CLASS_11.toInt()))
        items.add("Class " + AppConstants.CLASS_12 + CommonUtils.getClassSuffix(AppConstants.CLASS_12.toInt()))
        val namesArr = items.toTypedArray()

        showMultiSelectionDialog(items, mBinding.classToTeach, getString(R.string.class_to_teach), selectedClass)
    }

    fun onSelectOccupation() {
        val items = resources.getStringArray(R.array.occupation_arrays).toList()

        showSingleSelectionDialog(items, mBinding.selectOccupation, getString(R.string.select_ocupation), selectedOccupation)

    }

    fun onSelectExperience() {

        val items = resources.getStringArray(R.array.experience_arrays).toList()
        showSingleSelectionDialog(items, mBinding.teachingExperience, getString(R.string.select_treaching_epereience), selectedExp)
        //showExperience()
    }

    fun onSelectQualification() {
        selectedQualificationAreas.clear()
        mBinding.areaOfQualification.text = ""
        val items = resources.getStringArray(R.array.qualification_arrays).toList()
        showSingleSelectionDialog(items, mBinding.qualification, getString(R.string.select_qualification), selectedQualification)
    }

    fun onSelectQualificationArea() {
        lateinit var items: List<String>
        if (selectedQualification.size > 0) {
            if (selectedQualification[0].equals("Graduate")) {
                items = resources.getStringArray(R.array.area_qualifi_arrays_1).toList()

            } else if (selectedQualification[0].equals("Post-Graduate")) {
                items = resources.getStringArray(R.array.area_qualifi_arrays_2).toList()

            } else {
                items = resources.getStringArray(R.array.area_qualifi_arrays_1).toList()

            }

            showMultiSelectionDialog(items, mBinding.areaOfQualification, getString(R.string.select_area_of_qualification), selectedQualificationAreas)

        } else {
            showSnackError("Please select your qualification first.")

        }


    }

    fun onSelectTargetBoard() {
        val items = ArrayList<String>()
        items.add(AppConstants.BOARD_CBSE)
        items.add(AppConstants.BOARD_ICSE)
        items.add(AppConstants.BOARD_STATE)


        showMultiSelectionDialog(items, mBinding.targetedBoard, getString(R.string.select_targeted_board), selectedTargetBoard)
//        showTargetBoard()
    }

    fun showMultiSelectionDialog(items: List<String>, testview: TextView, title: String, selectedItems: ArrayList<String>) {
        val namesArr = items.toTypedArray()
        val booleans = BooleanArray(items.size)
        val selectedvalues = ArrayList<String>()

        for (i in selectedItems.indices) {
            if (items.contains(selectedItems[i])) {
                booleans[items.indexOf(selectedItems[i])] = true
                selectedvalues.add(selectedItems[i])
            }
        }

        AlertDialog.Builder(this)
                .setMultiChoiceItems(namesArr, booleans
                ) { _, i, b ->
                    if (b) {
                        selectedvalues.add(items[i])
                    } else {
                        selectedvalues.remove(items[i])
                    }
                }
                .setTitle(title)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    var selectedSubString = ""
                    for (i in selectedvalues.indices) {
                        selectedSubString += if (i == selectedvalues.size - 1) {
                            selectedvalues[i]
                        } else {
                            selectedvalues[i] + ","
                        }
                    }
                    testview.text = selectedSubString
                    selectedItems.clear()
                    selectedItems.addAll(selectedvalues)

                }
                .show()

    }

    private fun showSingleSelectionDialog(items: List<String>, testview: TextView, title: String, selectedItems: ArrayList<String>) {
        val namesArr = items.toTypedArray()
        var indexSelected = -1
        if (selectedItems.size > 0) {
            for (i in namesArr.indices) {
                if (namesArr[i].equals(selectedItems[0])) {
                    indexSelected = i
                    break
                } else {
                    indexSelected = 0
                }
            }
        } else {
            indexSelected = 0

        }


        AlertDialog.Builder(this)
                .setSingleChoiceItems(namesArr, indexSelected, null)
                .setTitle(title)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    var selectedPosition = (dialog as AlertDialog).listView.checkedItemPosition
                    if (selectedPosition < 0) {
                        selectedPosition = 0
                    }
                    testview.text = namesArr[selectedPosition]
                    selectedItems.clear()
                    selectedItems.add(namesArr[selectedPosition])
                    /*  mBinding.grade.text = namesArr[selectedPosition]
                      user!!.studentClass = (selectedPosition + 1).toString()*/
                }
                .show()
    }

    fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                        PermissionsRequest)
            }
        } else {
            onAddPicture()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PermissionsRequest -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                                && grantResults[1] == PackageManager.PERMISSION_GRANTED
                                && grantResults[2] == PackageManager.PERMISSION_GRANTED)) {
                    onAddPicture()
                }
            }
        }
    }

    fun onAddPicture() {

        TedBottomPicker.with(this)
                .show { uri ->
                    GlideApp.with(this)
                            .load(uri)
                            .placeholder(com.autohub.skln.R.drawable.default_pic)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)  // disable caching of glide
                            .skipMemoryCache(true)

                            .into(mBinding.profilePicture)
                    uploadImage(uri)

                }
    }


    /*  private fun showOccupation() {
          var items = getResources().getStringArray(R.array.occupation_arrays).toList()

          val namesArr = items.toTypedArray()
          val booleans = BooleanArray(items.size)
          val selectedOccupation = ArrayList<String>()

          val title: String
          title = "Choose Occupation"
          AlertDialog.Builder(this)
                  .setMultiChoiceItems(namesArr, booleans
                  ) { _, i, b ->
                      if (b) {
                          selectedOccupation.add(items[i])
                      } else {
                          selectedOccupation.remove(items[i])
                      }
                  }
                  .setTitle(title)
                  .setPositiveButton("OK") { dialog, _ ->
                      dialog.dismiss()
                      var selectedSubString = ""
                      for (i in selectedOccupation.indices) {
                          selectedSubString += if (i == selectedOccupation.size - 1) {
                              selectedOccupation[i]
                          } else {
                              selectedOccupation[i] + ","
                          }
                      }
                      mBinding.selectOccupation.text = selectedSubString

                  }
                  .show()

      }*/

    fun onBackClick() {
        onBackPressed()
    }


    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun setupProfile() {
        val ref = FirebaseStorage.getInstance().reference.child("tutor/" +
                "j9MtRdT5L0g62QiQ7z514z0hQz52"/*firebaseAuth.currentUser!!.uid*/ + ".jpg")
        GlideApp.with(this)
                .load(ref)
                .placeholder(com.autohub.skln.R.drawable.default_pic)
                .diskCacheStrategy(DiskCacheStrategy.NONE)  // disable caching of glide
                .skipMemoryCache(true)

                .into(mBinding.profilePicture)
    }

    private fun setUpUserInfo() {
        firebaseStore.collection(getString(R.string.db_root_tutors)).document("j9MtRdT5L0g62QiQ7z514z0hQz52"/*firebaseAuth.currentUser!!.uid*/).get()
                .addOnSuccessListener { documentSnapshot ->
                    val user = documentSnapshot.toObject(User::class.java)
//                    mUserViewModel!!.user = user!!
                }
                .addOnFailureListener { e -> showSnackError(e.message) }
    }


    private fun uploadImage(uri: Uri) {
        showLoading()
        val file = File(uri.path!!)
        val size = file.length().toInt()
        val bytes = ByteArray(size)
        try {
            val buf = BufferedInputStream(FileInputStream(file))
            buf.read(bytes, 0, bytes.size)
            buf.close()
            val picRef = mStorageReference!!.child("tutor/" + firebaseAuth.currentUser!!.uid + ".jpg")
            val uploadTask = picRef.putBytes(bytes)
            uploadTask.addOnSuccessListener {
                picRef.downloadUrl.addOnSuccessListener {
                    profilePictureUri = it.toString()
                }
                hideLoading()
            }.addOnFailureListener { e ->
                hideLoading()
                Toast.makeText(this, "Upload Failed -> $e", Toast.LENGTH_LONG).show()
            }

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            hideLoading()
        } catch (e: IOException) {
            e.printStackTrace()
            hideLoading()
        }
    }


    private fun editBio(bio: String) {
        val user = HashMap<String, Any>()
        user[AppConstants.KEY_BIODATA] = bio

        FirebaseFirestore.getInstance().collection(getString(R.string.db_root_tutors)).document(FirebaseAuth.getInstance().currentUser!!.uid).set(user, SetOptions.merge())
                .addOnSuccessListener {
                    //                    mUserViewModel!!.bioData = bio
                }
                .addOnFailureListener { }
    }


    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1122 && resultCode == Activity.RESULT_OK && data != null) {
            val croppedUri = data.getParcelableExtra<Uri>("_cropped_uri_")
            val originalUri = data.getParcelableExtra<Uri>("_original_uri_")
            Log.d(">>>RegisterAcRes", croppedUri!!.toString() + " , " + originalUri!!.toString())
            mBinding.profilePicture.setImageURI(croppedUri)
            mBinding.profilePicture.tag = croppedUri.path
            // uploadImage(croppedUri)
        }
    }

    fun makeSaveRequest() {
        if (isVerified()) {
            showLoading()

            val tutor = TutorData()

            tutor.qualification?.currentOccupation = mBinding.selectOccupation.text.toString()
            tutor.qualification?.experience = mBinding.teachingExperience.text.toString()
            tutor.qualification?.qualification = mBinding.qualification.text.toString()
            tutor.qualification?.qualificationArea = mBinding.areaOfQualification.text.toString()
            tutor.qualification?.targetBoard = mBinding.targetedBoard.text.toString()
            tutor.personInfo?.biodata = mBinding.bio.text.toString()
            if (profilePictureUri.trim().isNotEmpty()) {
                tutor.personInfo?.accountPicture = profilePictureUri
            } else {
                profilePictureUri = tutorData?.personInfo?.accountPicture!!
            }

            Log.e("tutor", tutor.toString())
            firebaseStore.collection(getString(R.string.db_root_tutors)).document(appPreferenceHelper.getuserID()).update(
                    "qualification.currentOccupation", tutor.qualification?.currentOccupation,
                    "qualification.experience", tutor.qualification?.experience,
                    "qualification.qualification", tutor.qualification?.qualification,
                    "qualification.qualificationArea", tutor.qualification?.qualificationArea,
                    "qualification.targetBoard", tutor.qualification?.targetBoard,
                    "personInfo.biodata", tutor.personInfo?.biodata,
                    "personInfo.accountPicture", tutor.personInfo?.accountPicture).addOnSuccessListener {
                hideLoading()
                showSnackError("Your profile is updated successfully!!")
            }.addOnFailureListener { e ->
                hideLoading()
                showSnackError(e.toString())
            }
        }
    }

    private fun isVerified(): Boolean {
        if (mBinding.classToTeach.text.isEmpty()) {
            showSnackError("Please select classes you teach.")
            return false
        } else if (subject_to_taught.text.isEmpty()) {
            showSnackError("Please select subjects you teach.")
            return false

        } else if (select_occupation.text.isEmpty()) {
            showSnackError("Please select your occupation.")
            return false

        } else if (teaching_experience.text.isEmpty()) {
            showSnackError("Please select your teaching experience.")
            return false

        } else if (qualification.text.isEmpty()) {
            showSnackError("Please select your qualification.")
            return false

        } else {
            return true
        }
    }
}