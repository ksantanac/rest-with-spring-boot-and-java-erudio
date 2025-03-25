package br.com.erudio.controllers;

import br.com.erudio.controllers.docs.EmailControllerDocs;
import br.com.erudio.data.dto.request.EmailRequestDTO;
import br.com.erudio.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/email/v1")
public class EmailController implements EmailControllerDocs {

    @Autowired
    private EmailService service;

    // SEND EMAIL
    @PostMapping
    @Override
    public ResponseEntity<String> sendEmail(EmailRequestDTO emailRequest) {
        service.sendSimpleEmail(emailRequest);

        return new ResponseEntity<>("E-mail sent with sucess!", HttpStatus.OK);
    }

    // SEND EMAIL WITH ATTACHMENT
    @Override
    public ResponseEntity<String> sendEmailWithAttachment(String emailResquestJson, MultipartFile multipartFile) {
        return null;
    }

}
