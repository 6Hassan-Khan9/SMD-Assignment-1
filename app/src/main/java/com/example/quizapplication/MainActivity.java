package com.example.quizapplication;

import java.util.Arrays;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private static final int TOTAL_QUESTIONS = 20;
    private int currentQuestionIndex = 0;
    private int remainingTime = 10 * 60000;
    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;

    private TextView questionTextView;
    private TextView timerTextView;
    private TextView scoreTextView;
    private RadioGroup optionsGroup;
    private Button nextButton;
    private Button prevButton;
    private Button showAnswerButton;
    private Button endExamButton;

    private int[] correctAnswers = {2, 2, 1, 0, 2, 2, 0, 0, 1, 0, 0, 1, 0, 2, 3, 1, 0, 2, 0, 1}; // Index of the correct option for each question
    private int[] questionScores = new int[TOTAL_QUESTIONS];
    private boolean[] answerShown = new boolean[TOTAL_QUESTIONS];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        questionTextView = findViewById(R.id.question_text);
        timerTextView = findViewById(R.id.timer);
        scoreTextView = findViewById(R.id.score);
        optionsGroup = findViewById(R.id.options_group);
        nextButton = findViewById(R.id.next_button);
        prevButton = findViewById(R.id.prev_button);
        showAnswerButton = findViewById(R.id.show_answer_button);
        endExamButton = findViewById(R.id.end_exam_button);

        updateQuestion();
        updateScore();

        startTimer();

        nextButton.setOnClickListener(v -> nextQuestion());
        prevButton.setOnClickListener(v -> prevQuestion());
        showAnswerButton.setOnClickListener(v -> showAnswer());
        endExamButton.setOnClickListener(v -> endExam());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void nextQuestion() {
        if (currentQuestionIndex < TOTAL_QUESTIONS - 1) {
            RadioButton selectedOption = findViewById(optionsGroup.getCheckedRadioButtonId());
            if (selectedOption != null) {
                int selectedOptionIndex = optionsGroup.indexOfChild(selectedOption);
                if (!answerShown[currentQuestionIndex]) {
                    if (selectedOptionIndex == correctAnswers[currentQuestionIndex]) {
                        questionScores[currentQuestionIndex] = 5;
                    } else {
                        questionScores[currentQuestionIndex] = -1;
                    }
                }
            }
            currentQuestionIndex++;
            optionsGroup.clearCheck();
            updateQuestion();
        }
    }

    private void prevQuestion() {
        if (currentQuestionIndex > 0) {
            RadioButton selectedOption = findViewById(optionsGroup.getCheckedRadioButtonId());
            if (selectedOption != null) {
                int selectedOptionIndex = optionsGroup.indexOfChild(selectedOption);

                if (!answerShown[currentQuestionIndex]) {
                    if (selectedOptionIndex == correctAnswers[currentQuestionIndex]) {
                        questionScores[currentQuestionIndex] = 5;
                    } else {
                        questionScores[currentQuestionIndex] = -1;
                    }
                }
            }
            currentQuestionIndex--;
            optionsGroup.clearCheck();
            updateQuestion();
        }
    }


    private void showAnswer() {
        if (!answerShown[currentQuestionIndex]) {
            int correctAnswerIndex = correctAnswers[currentQuestionIndex];
            RadioButton correctOption = (RadioButton) optionsGroup.getChildAt(correctAnswerIndex);
            correctOption.setChecked(true);
            questionScores[currentQuestionIndex] = -1;
            answerShown[currentQuestionIndex] = true;
            updateScore();
            Toast.makeText(this, "The correct answer is shown.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Answer already shown for this question.", Toast.LENGTH_SHORT).show();
        }
    }

    private void endExam() {
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }

        int totalMarks = Arrays.stream(questionScores).sum();
        int percentage = (totalMarks * 100) / (TOTAL_QUESTIONS * 5);

        scoreTextView.setVisibility(View.GONE);
        timerTextView.setVisibility(View.GONE);
        optionsGroup.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);
        prevButton.setVisibility(View.GONE);
        showAnswerButton.setVisibility(View.GONE);
        endExamButton.setVisibility(View.GONE);

        String resultText = "Total Marks: " + totalMarks + "\nPercentage: " + percentage + "%";
        questionTextView.setText(resultText);
        questionTextView.setTextSize(24);
        questionTextView.setGravity(View.TEXT_ALIGNMENT_CENTER);

        // Get the card view containing the question and options
        androidx.cardview.widget.CardView cardView = findViewById(R.id.question_card);

        // Update layout parameters to center the card in the screen
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) cardView.getLayoutParams();
        layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        cardView.setLayoutParams(layoutParams);
    }


    private void updateQuestion() {
        questionTextView.setText(getString(getResources().getIdentifier("question_" + currentQuestionIndex, "string", getPackageName())));

        // Set options
        for (int i = 0; i < 4; i++) {
            RadioButton optionButton = (RadioButton) optionsGroup.getChildAt(i);
            optionButton.setText(getString(getResources().getIdentifier("option_" + currentQuestionIndex + "_" + i, "string", getPackageName())));
        }
    }

    private void updateScore() {
        int minutes = (remainingTime / 1000) / 60;
        int seconds = (remainingTime / 1000) % 60;
        String timeFormatted = String.format("%02d:%02d", minutes, seconds);

        scoreTextView.setText("Score: " + Arrays.stream(questionScores).sum());
        timerTextView.setText("Time Left: " + timeFormatted);
    }

    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                remainingTime -= 1000;
                updateScore();
                if (remainingTime <= 0) {
                    endExam();
                } else {
                    timerHandler.postDelayed(this, 1000);
                }
            }
        };
        timerHandler.postDelayed(timerRunnable, 1000);
    }
}
