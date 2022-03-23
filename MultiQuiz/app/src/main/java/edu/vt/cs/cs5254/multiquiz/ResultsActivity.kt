package edu.vt.cs.cs5254.multiquiz

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import edu.vt.cs.cs5254.multiquiz.databinding.ActivityQuizBinding
import edu.vt.cs.cs5254.multiquiz.databinding.ActivityResultsBinding
import java.security.AccessControlContext

private const val EXTRA_TOTAL_CORRECT = "total_correct"
private const val EXTRA_TOTAL_QUESTIONS = "total_questions"
private const val EXTRA_TOTAL_HINT = "total_hint"

class ResultsActivity : AppCompatActivity() {

    lateinit var binding: ActivityResultsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        // ------------------------------------------------------
        // Create binding and content view
        // ------------------------------------------------------
        binding = ActivityResultsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // ------------------------------------------------------
        // Set current answer text
        // ------------------------------------------------------
        val totalQuestionsCount = intent.getIntExtra(EXTRA_TOTAL_QUESTIONS, -1)
        binding.totalQuestionsValue.text = totalQuestionsCount.toString()

        val correctAnswerCount = intent.getIntExtra(EXTRA_TOTAL_CORRECT, -1)
        binding.totalAnswersCorrectValue.text = correctAnswerCount.toString()

        val totalHintCount = intent.getIntExtra(EXTRA_TOTAL_HINT, -1)
        binding.totalHintsUsedValue.text = totalHintCount.toString()

    }

    // everything inside here (properties, functions, etc. is static
    companion object {
        fun newIntent(packageContext: Context,
                      totalQuestions: Int,
                      totalCorrect: Int,
                      totalHint: Int): Intent {
            return Intent(packageContext, ResultsActivity::class.java).apply {
                putExtra(EXTRA_TOTAL_QUESTIONS, totalQuestions)
                putExtra(EXTRA_TOTAL_CORRECT, totalCorrect)
                putExtra(EXTRA_TOTAL_HINT, totalHint)
            }
        }
    }
}