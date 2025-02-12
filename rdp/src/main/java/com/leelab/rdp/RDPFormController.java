package com.leelab.rdp;

import com.leelab.rdp.utils.ClassifierMain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class RDPFormController {
    @GetMapping("/classify")
    public String showForm(Model model) {
        RDPForm rdpform = new RDPForm();
        model.addAttribute("rdpform", rdpform);
        return "classify_form";
    }

    @PostMapping("/classify")
    public ResponseEntity<byte[]> classifyFASTA(@ModelAttribute("rdpform") RDPForm rdpForm)
    {
        try{
            byte[] result = ClassifierMain.classify(rdpForm);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("filename", "results.zip");
            return new ResponseEntity<>(result, headers, HttpStatus.OK);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
