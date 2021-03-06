package com.reportolia.core.web.controllers.table;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.reportolia.core.handler.db.DbHandler;
import com.reportolia.core.model.table.DbTable;
import com.reportolia.core.repository.table.DbTableColumnRepository;
import com.reportolia.core.repository.table.DbTableRepository;
import com.reportolia.core.web.controllers.base.BaseController;
import com.reportolia.core.web.controllers.base.datatable.JsonForm;

/**
 * 
 * Handles requests for the list show and save
 *
 * @author Batir Akhmerov
 * Created on Oct 23, 2016
 */
@Controller
public class TableListController  extends BaseController {
	
	@Resource protected DbHandler dbHandler;
	@Resource protected DbTableRepository tableRepository;
	@Resource protected DbTableColumnRepository tableColumnRepository;
	
	@RequestMapping(value = "/r3pTableListShow")
	public String show(Model model) {
		List<DbTable> list = this.dbHandler.getTableList(new JsonForm());
		model.addAttribute("isTableListEmpty", CollectionUtils.isEmpty(list));
		return "table/tableList";
	}

	
}
