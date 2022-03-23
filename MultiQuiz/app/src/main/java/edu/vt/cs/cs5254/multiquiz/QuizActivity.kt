package edu.vt.cs.cs5254.multiquiz

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.lifecycle.ViewModelProvider
import edu.vt.cs.cs5254.multiquiz.databinding.ActivityQuizBinding

private const val TAG = "QuizActivity"
private const val REQUEST_CODE_HINT = 0

class QuizActivity : AppCompatActivity() {


    private val DEFAULT_BUTTON_COLOR = "#16B896"
    private val SELECTED_BUTTON_COLOR = "#FF5733"

    lateinit var binding: ActivityQuizBinding

    // view fields (only one)
    lateinit var answerButtonList: List<Button>

    // access view model
    private val vm:QuizViewModel by lazy {
        ViewModelProvider(this).get(QuizViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate(Bundle?) called")

        // ------------------------------------------------------
        // Create binding and content view
        // ------------------------------------------------------
        binding = ActivityQuizBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // ------------------------------------------------------
        // Initialize answer-button list
        // ------------------------------------------------------

        answerButtonList = binding.answerButtons
            .children
            .toList()
            .filterIsInstance<Button>()

        // ------------------------------------------------------
        // Set text of views
        // ------------------------------------------------------

        binding.questionTextView.setText(R.string.iran_question)
        binding.questionTextView.setText(R.string.opera_question)
        binding.questionTextView.setText(R.string.africa_question)
        binding.questionTextView.setText(R.string.americas_question)

        // TODO Use pairs and a zipped list instead of 0..3
        vm.currentQuestionAnswer.zip(answerButtonList)
            .forEach { (answer, button) -> button.setText(answer.textResId)  }

        binding.hintButton.setText(R.string.hint_button)
        binding.submitButton.setText(R.string.submit_button)

        // ------------------------------------------------------
        // Add listeners to buttons
        // ------------------------------------------------------

        for (index in 0..3) {
            answerButtonList[index].setOnClickListener {
                processAnswerButtonClick(vm.currentQuestionAnswer[index])
            }
        }



        binding.hintButton.setOnClickListener {
            processHintButtonClick()
        }
        binding.submitButton.setOnClickListener {
            processSubmitButtonClick()
        }

        // ------------------------------------------------------
        // Refresh the view
        // ------------------------------------------------------

        refreshView()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart() called")
    }
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() called")
    }
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause() called")
    }
    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop() called")
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
    }

    private fun processAnswerButtonClick(clickedAnswer: Answer) {

        val origIsSelected = clickedAnswer.isSelected

        // TODO Use forEach instead of for loop
        vm.currentQuestionAnswer.minus(clickedAnswer).forEach { it.isSelected = false }

        clickedAnswer.isSelected = !origIsSelected
        binding.submitButton.isEnabled = false
        if ((clickedAnswer.isSelected)) {
            binding.submitButton.isEnabled = true
        }

        refreshView()
    }

    private fun processHintButtonClick() {

        vm.giveHint()

        if (vm.shouldSubmitDisabled()) {
            binding.submitButton.isEnabled = false
        }

        refreshView()
    }

    private fun processSubmitButtonClick() {

        vm.countCorrectAnswers()
            if (vm.hasMoreQuestions()) {
                for (answer in vm.currentQuestionAnswer) {
                    answer.isEnabled = true
                    answer.isSelected = false
                }
                vm.moveToNext()
            } else {
                vm.currentQuestionAnswer.forEach { answer -> answer.isSelected = false }
                val intent = ResultsActivity.newIntent(this@QuizActivity, vm.totalQuestions, vm.correctCount, vm.hintCount )
                startActivity(intent)
            }

        binding.hintButton.isEnabled = true
        binding.submitButton.isEnabled = false


        refreshView()
    }

    private fun refreshView() {

        // ------------------------------------------------------
        // Set text of question and answer buttons
        // ------------------------------------------------------

        binding.questionTextView.setText(vm.questionText)

        // TODO Use pairs and a zipped list instead of 0..3
        vm.currentQuestionAnswer.zip(answerButtonList).forEach { (answer, button) -> button.setText(answer.textResId)  }


        // TODO Use pairs and a zipped list instead of 0..3
        vm.currentQuestionAnswer.zip(answerButtonList).forEach { (answer, button) ->
            button.isEnabled = answer.isEnabled
            button.isSelected = answer.isSelected
            if (answer.isSelected) {
                setButtonColor(button, SELECTED_BUTTON_COLOR)
                binding.submitButton.isEnabled = true

            } else {
                setButtonColor(button, DEFAULT_BUTTON_COLOR)

            }
            if (!answer.isEnabled) {
                button.alpha=.5f
            }
        }

        binding.hintButton.isEnabled = vm.shouldHintBeEnabled()
        binding.submitButton.isEnabled = vm.shouldSubmitDisabled()

    }


    private fun setButtonColor(button: Button, colorString: String) {
        button.backgroundTintList =
            ColorStateList.valueOf(Color.parseColor(colorString))
        button.setTextColor(Color.WHITE)
        button.alpha = 1f
    }
}
