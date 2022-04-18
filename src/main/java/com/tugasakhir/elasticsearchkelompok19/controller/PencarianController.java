package com.tugasakhir.elasticsearchkelompok19.controller;

import com.tugasakhir.elasticsearchkelompok19.model.PDFDocument;
import com.tugasakhir.elasticsearchkelompok19.services.DocumentServices;
import com.tugasakhir.elasticsearchkelompok19.services.SearchServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

@Controller
@RequestMapping("/pencarian")
public class PencarianController {

    /*Logger*/
    final Logger log = LoggerFactory.getLogger(PencarianController.class);

    /*List of Documents*/
    List<PDFDocument> listPdf = null;

    @Autowired
    SearchServices services;

    @Autowired
    DocumentServices documentServices;

    @GetMapping
    public String showPencarian(){
        return "Pencarian";
    }

    @GetMapping(value = "/search")
    public ModelAndView search(
            @RequestParam("keyword") String keyword,
            ModelMap model
    ) {

        log.info("Searching for keyword : " + keyword);
        listPdf = services.fullTextSearch(keyword);

        if(listPdf != null){
            model.addAttribute("keyword", keyword);
            model.addAttribute("listPdf", listPdf);
            log.info("Got " + listPdf.size() + " PDF Data!");
        }else{
            model.addAttribute("keyword", keyword);
            model.addAttribute("listPdf", "empty");
            log.info("No Data Found!");
        }

        return new ModelAndView("forward:/pencarian",model);
    }

    @GetMapping(value = "/showFile/{docId}",produces = MediaType.APPLICATION_PDF_VALUE)
    @ResponseBody
    public ResponseEntity<?> showPdf(@PathVariable("docId") String documentId){
        HttpHeaders headers = new HttpHeaders();
        try{
            File pdfFile = documentServices.getFile(documentId);
            if(pdfFile != null){
                InputStream fileToOpen = new FileInputStream(pdfFile);
                log.info("Showing PDF File : " + documentId);
                InputStreamResource resource = new InputStreamResource(fileToOpen);
                headers.add("content-disposition","inline; filename=" + documentId);
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(resource);
            }
        }catch (Exception e){
            log.info("FILE OPERATION ERROR : " + e.getMessage());
        }
        headers.add("Location","/");
        return new ResponseEntity<byte[]>(null,headers, HttpStatus.FOUND);
    }
    
}
