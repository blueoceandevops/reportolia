package com.reportolia.core.web.controllers.table;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.reportolia.core.handler.db.DbHandler;
import com.reportolia.core.handler.db.relationship.RelationshipInfo;
import com.reportolia.core.repository.table.DbTableRelationshipRepository;
import com.reportolia.core.repository.table.DbTableRepository;
import com.reportolia.core.web.controllers.jtable.BaseJsonResult;
import com.reportolia.core.web.controllers.jtable.JsonResult;

/**
 * 
 * Handles requests for the relationship bean show, save and delete
 *
 * @author Batir Akhmerov
 * Created on Oct 23, 2016
 */
@Controller
public class RelationshipController {
	
	@Resource protected DbHandler dbHandler;
	@Resource protected DbTableRepository tableRepository;
	@Resource protected DbTableRelationshipRepository tableRelationshipRepository;
	
	
	@RequestMapping(value = "/r3pTableRelationshipsShow")
	public ModelAndView relationshipsShow(long tableId, ModelAndView mav) {
		mav.setViewName("table/relationshipList");
		mav.getModel().put("currentTable", this.tableRepository.findById(tableId));
		return mav;
	}
	
	@ResponseBody
	@RequestMapping(value = "/r3pTableRelationshipsLoad")
	public JsonResult<RelationshipInfo> relationshipsLoad(long tableId) {
		return new JsonResult<RelationshipInfo>(this.dbHandler.getTableRelationshipInfoList(tableId));
	}
	
	@ResponseBody
	@RequestMapping(value = "/r3pTableRelationshipSave")
	public JsonResult<RelationshipInfo> relationshipSave(@RequestBody RelationshipInfo info) {
		return new JsonResult<RelationshipInfo>(this.dbHandler.saveTableRelationship(info));
	}
	
	@ResponseBody
	@RequestMapping(value = "/r3pTableRelationshipDelete")
	public BaseJsonResult relationshipDelete(@RequestBody RelationshipInfo info) {
		this.dbHandler.deleteTableRelationship(info);
		return new BaseJsonResult();
	}
	
}
