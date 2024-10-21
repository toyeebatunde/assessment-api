package co.payrail.nddc_assessment.assessment.controller;

import co.payrail.nddc_assessment.assessment.dto.input.QuestionDTO;
import co.payrail.nddc_assessment.assessment.entity.Assessment;
import co.payrail.nddc_assessment.assessment.entity.Question;
import co.payrail.nddc_assessment.assessment.service.QuestionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/question")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    // Endpoint to create a question for a specific assessment
    @PostMapping("/create/{assessmentId}")
    public ResponseEntity<Question> createQuestion(
            @PathVariable Long assessmentId,
            @RequestBody QuestionDTO questionDTO) throws JsonProcessingException {

        Question question = questionService.createQuestion(assessmentId, questionDTO);
        return ResponseEntity.ok(question);

    }

    @GetMapping("/list/{assessmentId}")
    public ResponseEntity<List<Question>> listQuestions(@PathVariable Long assessmentId) {
        return ResponseEntity.ok(questionService.getListQuestions(assessmentId));
    }


}
