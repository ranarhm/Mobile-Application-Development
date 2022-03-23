package edu.vt.cs.cs5254.multiquiz

import androidx.lifecycle.ViewModel

class QuizViewModel : ViewModel() {

    private val questionBank = listOf(
        Question(
            R.string.iran_question,
            listOf(
                Answer(R.string.iran_answer_tehran, true),
                Answer(R.string.iran_answer_isfahan, false),
                Answer(R.string.iran_answer_shiraz, false),
                Answer(R.string.iran_answer_yazd, false)
            )
        ),
        Question(
            R.string.opera_question,
            listOf(
                Answer(R.string.opera_answer_brisbane, false),
                Answer(R.string.opera_answer_sydney, true),
                Answer(R.string.opera_answer_perth, false),
                Answer(R.string.opera_answer_canberra, false)
            )
        ),
        Question(
            R.string.africa_question,
            listOf(
                Answer(R.string.africa_answer_nigeria, false),
                Answer(R.string.africa_answer_libya, false),
                Answer(R.string.africa_answer_egypt, true),
                Answer(R.string.africa_answer_morocco, false)
            )
        ),
        Question(
            R.string.americas_question,
            listOf(
                Answer(R.string.americas_answer_missouri, false),
                Answer(R.string.americas_answer_mississippi, false),
                Answer(R.string.americas_answer_purus, false),
                Answer(R.string.americas_answer_amazon, true)
            )
        )
    )
    var questionIndex = 0
    var hintCount = 0
    var correctCount = 0

    val questionText
        get() = questionBank[questionIndex].textResId

    val currentQuestionAnswer
        get() = questionBank[questionIndex].answerList

    val totalQuestions
        get() = questionBank.size

    fun moveToNext() {
        questionIndex=(questionIndex + 1) % questionBank.size
    }

    fun hasMoreQuestions() = questionIndex < 3

    fun giveHint() {
        val randomAnswer = currentQuestionAnswer
            .filter { it.isEnabled }
            .filterNot { it.isCorrect }
            .random()

        randomAnswer.isEnabled = false
        randomAnswer.isSelected = false
        hintCount++

    }

    fun shouldHintBeEnabled(): Boolean {
                if (currentQuestionAnswer.filter { it.isEnabled }
                .filterNot { it.isCorrect }
                .count() == 0) {
                    return false
        }
        return true
    }

    fun shouldSubmitDisabled(): Boolean {
        currentQuestionAnswer.forEach { if (it.isSelected) {
                return true
            }
        }

        return false
    }

    fun countCorrectAnswers() {
        val selectedAnswer =
            currentQuestionAnswer.first { it.isSelected }
        if (selectedAnswer.isCorrect) {
            correctCount++
        }
    }

    fun resetAnswers() {
        currentQuestionAnswer
            .forEach {
                it.isEnabled
                !it.isSelected
            }
    }

}