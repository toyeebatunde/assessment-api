package co.payrail.nddc_assessment.assessment.service;

import co.payrail.nddc_assessment.assessment.dto.enums.QuestionType;
import co.payrail.nddc_assessment.assessment.dto.input.QuestionDTO;
import co.payrail.nddc_assessment.assessment.entity.Assessment;
import co.payrail.nddc_assessment.assessment.entity.Option;
import co.payrail.nddc_assessment.assessment.entity.Question;
import co.payrail.nddc_assessment.assessment.repository.AssessmentRepository;
import co.payrail.nddc_assessment.assessment.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AssessmentRepository assessmentRepository;

    // Method to create a question for an assessment
    public Question createQuestion(Long assessmentId, QuestionDTO questionDTO) {
        // Ensure the assessment exists
        assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        // Create the question
        Question question = new Question();
        question.setAssessmentId(assessmentId);
        question.setQuestionText(questionDTO.getQuestionText());
        question.setQuestionType(questionDTO.getQuestionType());

        // Handle options for MCQ or MULTI_CORRECT
        if (questionDTO.getQuestionType() == QuestionType.MCQ || questionDTO.getQuestionType() == QuestionType.MULTI_CORRECT) {
            List<Option> options = questionDTO.getOptions();
            if (options == null || options.isEmpty()) {
                throw new IllegalArgumentException("Options cannot be null or empty for MCQ or MULTI_CORRECT questions");
            }
            System.out.println(questionDTO.getOptions());  // Log options before saving

            // Set the options for the question
            question.setOptions(options);
        }

        // Handle short answer type
        if (questionDTO.getQuestionType() == QuestionType.SHORT_ANSWER) {
            question.setCorrectAnswer(questionDTO.getCorrectAnswer());
        }

        // Handle TRUE_FALSE questions
        if (questionDTO.getQuestionType() == QuestionType.TRUE_FALSE) {
            Option trueOption = new Option("True", questionDTO.isCorrectTrueFalse());
            Option falseOption = new Option("False", !questionDTO.isCorrectTrueFalse());
            question.setOptions(List.of(trueOption, falseOption));  // Set two options for TRUE/FALSE
        }

        // Save and return the created question
        return questionRepository.save(question);
    }




    public List<Question> getListQuestions(Long assessmentId) {
       return questionRepository.findByAssessmentId(assessmentId);
    }
}
