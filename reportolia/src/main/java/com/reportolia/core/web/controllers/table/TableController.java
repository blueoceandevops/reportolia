package com.reportolia.core.web.controllers.table;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.reportolia.core.handler.db.DbHandler;
import com.reportolia.core.model.table.DbTable;
import com.reportolia.core.model.table.DbTableColumn;
import com.reportolia.core.repository.table.DbTableColumnRepository;
import com.reportolia.core.repository.table.DbTableRepository;
import com.reportolia.core.utils.CoreUtils;
import com.reportolia.core.web.controllers.base.BaseController;

/**
 * 
 * Handles requests for the list show and save
 *
 * @author Batir Akhmerov
 * Created on Oct 23, 2016
 */
@Controller
public class TableController  extends BaseController {
	
	@Resource protected DbHandler dbHandler;
	@Resource protected DbTableRepository tableRepository;
	@Resource protected DbTableColumnRepository tableColumnRepository;
	
	@ModelAttribute("dbTable")
	public DbTable getBean(Long id) {
		if (CoreUtils.isKeyNull(id)) {
			return new DbTable();
		}
		return this.tableRepository.findById(id);
	}
	
	@RequestMapping(value = "/r3pTableShow")
	public String show(Model model, @ModelAttribute("dbTable") DbTable dbTable) {
		List<DbTableColumn> list = this.tableColumnRepository.findByDbTable(dbTable);
		model.addAttribute("columnList", CollectionUtils.isEmpty(list));
		return "table/tableBean";
	}
	
	@RequestMapping(value = "/r3pTableSave")
	public String save( @ModelAttribute("dbTable") DbTable dbTable, RedirectAttributes redir) {
		this.dbHandler.saveTable(dbTable);
		redir.addAttribute("id", dbTable.getId());
		return "redirect:r3pTableShow.go";
	}

	
}
