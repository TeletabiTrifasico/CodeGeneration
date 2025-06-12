package com.codegeneration.banking.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;

@CrossOrigin(
        origins = {
                "http://localhost:3000",
                "http://localhost:8080",
                "https://teletabitrifasico.github.io",
                "https://codegen.xaff.dev",
                "http://10.10.11.254:8080"
        },
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
        allowCredentials = "true",
        maxAge = 3600
)
public class BaseController {
}
