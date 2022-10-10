package com.inventi.bankstatementsapi.controller;

import com.inventi.bankstatementsapi.dto.AccountBalanceDto;
import com.inventi.bankstatementsapi.dto.BankStatementDto;
import com.inventi.bankstatementsapi.exception.CsvExportFailedException;
import com.inventi.bankstatementsapi.service.BankStatementService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/bank-statements")
public class BankStatementController {

    private final BankStatementService bankStatementService;

    @PostMapping("/import")
    public ResponseEntity<List<BankStatementDto>> importBankStatementsToCsv(@RequestParam("file") MultipartFile file) {
        List<BankStatementDto> bankStatementDtos = bankStatementService.importFromCsv(file);
        return ResponseEntity.ok(bankStatementDtos);
    }

    @GetMapping("/export")
    public void exportBankStatementsToCsv(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                          HttpServletResponse httpServletResponse) {
        httpServletResponse.setContentType("text/csv");
        httpServletResponse.addHeader("Content-Disposition", "attachment; filename=\"bank-statements.csv\"");
        try {
            bankStatementService.exportToCsv(from, to, httpServletResponse.getWriter());
        } catch (IOException e) {
            throw new CsvExportFailedException(e);
        }
    }

    @GetMapping("/balance")
    public ResponseEntity<List<AccountBalanceDto>> getAccountBalances(@RequestParam @NotEmpty String accountNumber,
                                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<AccountBalanceDto> accountBalances = bankStatementService.getAccountBalances(accountNumber, from, to);
        return ResponseEntity.ok(accountBalances);
    }
}
