package com.bharadwaj.demoone.controller;

import com.bharadwaj.demoone.dto.PlanResponse;
import com.bharadwaj.demoone.model.Plan;
import com.bharadwaj.demoone.service.MedicalPlanService;
import com.bharadwaj.demoone.util.EtagUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
@RequestMapping("/v1/plan")
public class MedicalPlanController {

    @Autowired
    private MedicalPlanService medicalPlanService;

    @PostMapping
    public ResponseEntity<PlanResponse> saveMedicalPlan(@Valid @RequestBody Plan p){
        boolean result = medicalPlanService.savePlan(p);
        if(result && p.getObjectId() != null){
            HttpHeaders responseHeaders = new HttpHeaders();
            String eTag = "\"" + EtagUtil.generateEtag(p) + "\"";
            responseHeaders.setETag(eTag);
            return ResponseEntity.status(201).headers(responseHeaders).body(new PlanResponse(p.getObjectId()));
        }else{
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return null;
    }


    @GetMapping
    public ResponseEntity<List<Plan>> fetchAllPlans(){
        List<Plan> plans;
        plans = medicalPlanService.fetchAllPlans();
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/{objectId}")
    public ResponseEntity<Plan> getMedicalPlan(@PathVariable String objectId,
                                               @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch){
        Optional<Plan> plan = medicalPlanService.getMedicalPlanById(objectId);
        if(plan.isPresent()){
            String currentETag = "\"" + EtagUtil.generateEtag(plan.get()) + "\"";
            if(ifNoneMatch != null && currentETag.equals(ifNoneMatch)){
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
            }
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setETag(currentETag);

            return ResponseEntity.ok().headers(responseHeaders).body(plan.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{objectId}")
    public ResponseEntity<String> deleteMedicalPlan(@PathVariable String objectId){
        log.info("Object passed to delete Medical Plan : " + objectId);
        Optional<Boolean> result = medicalPlanService.deletePlan(objectId);
        if(result.isPresent()){
            if(result.get()){
                return ResponseEntity.noContent().build();
            }else{
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(String.format("Plan with Object ID : %s Not found", objectId));
        }
    }
}
