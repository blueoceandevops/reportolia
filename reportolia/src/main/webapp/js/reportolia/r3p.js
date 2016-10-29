var r3p = (function(){
	var _isPageModified = false
		;
	
	// validation
	if (!self.form2js) {
		alert('form2js utility is expected!');
		return;
	}
	
	
	
	// protected methods
	function _form2JsInterceptor(node) {
		var $node = $(node),
			tagName = $node.prop('tagName');
			name = $node.attr('name') || '';
		tagName = tagName || '';
		
		if (tagName.toLowerCase() == 'input') {
			var type = $node.prop('type');
			type = type || '';
			
			if (type.toLowerCase() == 'checkbox') {
				return {name: name, value: $node.prop('checked')};
			}
		}
		/*
        var dataName = node.getAttribute ? node.getAttribute('data-name') : '',
            dayNode,
            monthNode,
            yearNode,
            day,
            year,
            month;

        if (dataName && dataName != '' && node.className == 'datefield')
        {
            dayNode = node.querySelector('input[name="'+dataName + '.day"]');
            monthNode = node.querySelector('select[name="'+dataName + '.month"]');
            yearNode = node.querySelector('input[name="'+dataName + '.year"]');

            day = dayNode.value;
            year = yearNode.value;
            month = monthNode.value;

            return { name: dataName, value:  year + '-' + month + '-' + day};
        }
		*/
        return false;
    };
    
    
    
	
	
	
	/*
	<div id="dialog-confirm" title="Empty the recycle bin?">
	  <p><span class="ui-icon ui-icon-alert" style="float:left; margin:12px 12px 20px 0;"></span>These items will be permanently deleted and cannot be recovered. Are you sure?</p>
	</div>
	*/
	
    // public methods
	return {
		
		/******************************************/
	    /**   AJAX   ***************************/
	    /******************************************/
		ajax: function(conf, fnSuccess, fnError) {
			var data = conf.json || {};
			delete conf.json;
			
			conf = $.extend({
					contentType: 'application/json;charset=UTF-8',
					dataType: 'json',
					type : 'POST',
					processData : false,
					cache: false,
			        data: JSON.stringify(data)
				}, conf);
			
			$.ajax(conf)
				.done(function(resp) { 
					//alert("Success: " + data.param1) ;
					if (fnSuccess) fnSuccess(resp);
				})
				.fail(function(resp) {
					if (fnError) fnError(resp);
					else alert("Error: " + (resp && resp.error ? resp.error : 'Unexpected'));
				});
		},
		
		/******************************************/
	    /**   DIALOGS   ***************************/
	    /******************************************/		
		showConfirm: function(msg, title, conf, fnOk, fnCancel) {
			conf = conf || {};
			var div = r3p.createDialogDiv('dlg-confirm', title || r3pMsg.TLT_CONFIRM, msg);
			conf.buttons = conf.buttons || {
				Ok: function() {
					if (fnOk) fnOk();
					r3p.closeDlg(this);
				},
				Cancel: function() {
					if (fnCancel) fnCancel();
					r3p.closeDlg(this);
				}
			};
			
			conf.id = div.attr('id');
			r3p.showDialog(conf);
		},
		
		showError: function(msg, title, conf, fnOk, fnCancel) {
			conf = conf || {};
			var div = r3p.createDialogDiv('dlg-error', title || r3pMsg.TLT_ERROR, msg);
			conf.buttons = conf.buttons || {
				Ok: function() {
					if (fnOk) fnOk();
					r3p.closeDlg(this);
				}
			};
			conf.id = div.attr('id');
			r3p.showDialog(conf);
		},
		
		showDialog: function(conf) {
			conf = $.extend({
				resizable: false,
				height: "auto",
				width:400,
				modal: true,
				buttons: {
					Ok: function() {
						$(this).dialog('close');
					}
				}
			}, conf);
			$('#' + conf.id).dialog(conf);
		},
		
		closeDlg: function(div) {
			$(div).dialog('close');
		},
		
		/******************************************/
	    /**   FORM   ***************************/
	    /******************************************/
		form2js: function(node) {
			return form2js(node, '.', true, _form2JsInterceptor);
		},
		
		urlToJson: function(url) {
			if (url) { 
				return JSON.parse(
					'{"' + url.replace(/&/g, '","').replace(/=/g,'":"') + '"}',
					function(key, value) { 
						return key==='' ? value : decodeURIComponent(value);
					}
				);
			}
			return {};
		},
		urlToJsonSafe: function(urlOrJson) {
			var json = urlOrJson;
			if (r3p.isString(urlOrJson)) json = r3p.urlToJson(urlOrJson);
			return json;
		},
		
		
		/******************************************/
	    /**   HTML   ***************************/
	    /******************************************/
		createEl: function(tag, parent, attrs, props, cls) {
	    	var el = $('<' + tag + '></' + tag + '>');
	    	if (parent) el.appendTo(parent);
	    	if (attrs) {
	    		$.each(attrs, function(name, val) {
	    			if (attrs.hasOwnProperty(name)) el.attr(name, val);
	    		});
	    	}
	    	if (props) {
	    		$.each(props, function(name, val) {
	    			if (attrs.hasOwnProperty(name)) el.prop(name, val);
	    		});
	    	}
	    	if (cls) el.addClass(cls);
	    	return el;
	    },
	    
	    createDialogDiv: function (id, title, msg) {
			var div = r3p.jq(id);
			if (r3p.isNullJq(div)) {
				div = r3p.createEl('div', 'body', {id: id}, null, 'clsHidden');
				r3p.createEl('p', div);
			}
			div.children('p').html(msg);
			div.attr('title', r3p.scriptSafe(title));
			return div;
		},
		
		sticky: function(el, bottomGap, rightGap) {
			el = $(el);
			//var offset = el.offset();
			if (r3p.isNotNull(bottomGap)) {
				//console.log(wnd.height());
				el.height($(window).height() - el.offset().top - bottomGap);
			}
			if (r3p.isNotNull(rightGap)) {
				el.width($(window).width() - el.offset().left - rightGap);
			}
		},
		
		onWindowResize: function(fnResize) {
			$(window).resize(
				r3p.getSafeTimeout(fnResize, {delay: 10})
			);
		},
		
		/******************************************/
	    /**   JTABLE   ***************************/
	    /******************************************/
		
		jTable: function(id, conf) {
			conf = conf || {};
			var fnRecLoaded = conf.recordsLoaded;
			conf.recordsLoaded = function(evt, data){
				if (fnRecLoaded) fnRecLoaded.call(this, evt, data);
				var tbl = $(this);
				if (r3p.isNotNull(conf.height) && !tbl.prop('isScrollInitialized')) {
            		r3p.jTableScrollable(tbl, conf.height);
            		tbl.prop('isScrollInitialized', true);
            	}
            };
            
            var defConfig = {
            	jqueryuiTheme: true,
            	messages: r3pJtableMsg,
            	defaultDateFormat: r3pJtableMsg.defaultDateFormat
            };
			conf = $.extend({}, defConfig, conf);
						
			if (conf.actions && conf.actions.isJsonActions) {
				var acts = conf.actions;
				acts.defJson = acts.defJson || {};
				if (r3p.isString(acts.createAction)) {
                	acts.createAction = r3p.jtableAjaxAction(acts.createAction, r3p.urlToJsonSafe, acts.defJsonCreate || acts.defJson);
                }
                if (r3p.isString(acts.deleteAction)) {
                	acts.deleteAction = r3p.jtableAjaxAction(acts.deleteAction, r3p.urlToJsonSafe, acts.defJsonDelete || acts.defJson);
                }
                if (r3p.isString(acts.updateAction)) {
                	acts.updateAction = r3p.jtableAjaxAction(acts.updateAction, r3p.urlToJsonSafe, acts.defJsonUpdate ||  acts.defJson);
                	if (r3p.isNull(acts.createAction)) acts.createAction = acts.updateAction;
                }
			}
			
			r3p.jq(id).jtable(conf);
		},
		
		jTableScrollable: function(div, height) {
			if (r3p.isNullJq(div)) return;
			var container = div.find('.jtable-main-container'),
				tbl = container.find('.jtable'),
				thead = tbl.find('thead'),
				divWrapper = r3p.createEl('div', null, null, null, 'jtable-wrapper');
			
			tbl.before(divWrapper);
			divWrapper.append(tbl);
			
			divWrapper.height(height);
			
			tbl.floatThead({
				scrollContainer: function($table){
					return tbl.closest('.jtable-wrapper');
				}
			});
		},
			
		jtableAjaxAction: function(url, fnJsonCallback, jsonDefaults) {
			return function(formParams, jtParams) {
				return $.Deferred(function($dfd) {
					var json = fnJsonCallback ? fnJsonCallback(formParams) : {};
					if (jsonDefaults)  {
						var tmpDefaults = jsonDefaults;
						if ($.isFunction(jsonDefaults))  tmpDefaults = jsonDefaults();
						json = $.extend({}, tmpDefaults, json);
					}
					r3p.ajax({
							url: url + '?id=' + json.id,
							json: json
						}, 
						function(data) {
							if (data && data.Result == 'ERROR') {
								data.Message = data.Message || 'Unexpected Error!';
							}
							r3p.setPageModified();
							$dfd.resolve(data);
						}, 
						function() {
							$dfd.reject();
						}
					);
				});
	        };
		},
	    
	    /******************************************/
	    /**   PAGE   ***************************/
	    /******************************************/
		navigate: function(url) {
			window.location.href = url;
		},
		openWindow: function(url) {
			
		},
		isPageModified: function() {
			return _isPageModified;
		},
		setPageModified: function(modified) {
			_isPageModified = r3p.isNull(modified) ? true : modified;
		},
		
		/******************************************/
	    /**   UTILS   ***************************/
	    /******************************************/
		jq: function(idName) {
			idName = idName.replace( /(:|\.|\[|\]|,|=)/g, '\\$1' );
			var el = $('#' + idName);
			if (el.length == 0) el = $('[name=' + idName + ']');
			return el;
		},
		isEmpty: function(list) {
			return r3p.isNull(list) || list.length == 0;
		},
		isNullJq: function(el) {
			return !el || el.length == 0;
		},
		isNull: function(o) {
			return !o || o == undefined;
		},
		isNotNull: function(o) {
			return !r3p.isNull(o);
		},
		isString: function(v) {
			return $.type(v) == 'string';
		},
		scriptSafe: function(txt) {
			return $('<div/>').text(txt).html();
		},
		findByProperty: function(list, propName, value) {
			if (r3p.isEmpty(list)) return null;
			var res = null;
			$.each(list, function(i, o){
				if (o[propName] == value) {
					res = o;
					return false;
				}
			});
			return res;
		},
		getSafeTimeout: function(fnHandler, conf) {
			if (!conf) {
				alert('Argument conf is required!');
			}
			return function() {
				if (conf.isSet) return;
				conf.isSet = setTimeout(function(){
					fnHandler();
					conf.isSet = null;
				}, conf.delay || 200);
			};
		},
		
		coalesceObjFn: function() {
			var res = null;
			for (var ind in arguments) {
				if (!arguments.hasOwnProperty(ind)) continue;
				var objFn = arguments[ind];
				if (!p3r.isNotNull(objFn) && $.isFunction(objFn)) {
					objFn = objFn();
				}
				if (!p3r.isNotNull(objFn)) {
					res = objFn;
					break;
				}
			}
			return res;
		}
		
		/*,
		renameJsonField: function(json, fld, newFld) {
			if (!json || json[fld] === undefined) return;
			json[newFld] = json[fld];
			delete json[fld];
		},
		subJsonField: function(json, fld, subFld) {
			if (!json || json[fld] === undefined) return;
			var v = json[fld];
			json[fld] = {};
			json[fld][subFld] = v;
		}
		*/
	};
}());

/**
 * List Form plugin encapsulates utilities to save/restore indexed form data from items of the html container.
 * html:
 * <form>
 * 		<div>
 * 			<input name="id" value="1" />
 * 			<input name="name" value="cat" />
 * 			<input name="type" value="animal" />
 * 		</div>
 * 		<div>
 * 			<input name="id" value="2" />
 * 			<input name="name" value="dog" />
 * 			<input name="type" value="animal" />
 * 		</div>
 * </form>
 * 
 * js:
 * 	var form = $('form').r3pListForm({
 * 		url: 'saveJsonAjax.go',
 * 		data: errorData, // form data from previous session. Used only in case of error.
 * 		jsonField: 'json', // default
 * 		itemSelector: 'div', // default
 * 		idField: 'id', // default, points to form > div > input[name=id]
 * 	});
 * 
 * saving data:
 * 	form.save();
 * 
 * sends json object:
 * 	json = {
 * 		list: [
 * 			{id: 1, name: 'cat', type: 'animal'},
 * 			{id: 2, name: 'dog', type: 'animal'}
 * 		],
 * 		removedList: [
 * 			{id: 3} // in case if item was removed
 * 		]
 * 	}
 * 
 */
(function($){
	$.fn.r3pListForm = function(conf) {
		if (!conf) return;
		
		var defaults = $.fn.r3pListForm.defaults;
		var o = $.extend({}, defaults, conf);
		return new listForm(this, o);
	};
	
	$.fn.r3pListForm.defaults = {
		jsonField: 'json', 
		itemSelector: 'div', 
		idField: 'id'
	};
	
	function listForm(form, conf){
		var that = this;
		
		// protected functions
		function _getItems() {
			return form.children(conf.itemSelector);
		};
		
		function _init() {
			if (items.length > 0) {
				$.each(items, function(i, itm){
					
				});
			}
		};
		
		function _toJson() {
			var items = _getItems();
				json = {};
			json['list'] = [];
			json['removedList'] = [];
			
			if (conf.fnAddToList) json.list = conf.fnAddToList(items);
			else if (items.length == 0) {			
				$.each(items, function(i, itm){
					json.list.push( r3p.form2js($(itm).get(0)) );
				});
			}
			
			if (conf.fnAddToRemovedList) json.removedList = conf.fnAddToRemovedList(items);
			
			
			return json;	
		};
		
		/*
		this.submitForm = function(f){
			if (!f) return;
			var jsonField = $('[name=json');
			if (jsonField.length == 0) {
				jsonField = $('<input></input>').attr('type', 'hidden').attr('name', 'json').appendTo(f);
			}
			jsonField.val(JSON.stringify(_getJson()));
			f.submit(); 
		};
		*/
		
		// public functions
		this.save = function(fnSuccess, fnError){
			var json = _toJson();
			r3p.ajax({url: conf.url, json: json}, fnSuccess, fnError);
		};
		
		
		
		return this;
	}
})(jQuery);

/*
(function($){
	$.fn.r3pListForm = function(conf) {
		if (!conf) return;
		var defaults = $.fn.r3pListForm.defaults;
		var o = $.extend({}, defaults, conf);
		return new listForm(this, o);
	};
	
	$.fn.r3pListForm.defaults = {
			
	};
	
	function listForm(form, conf){
		var that = this;
		
		return this;
	}
})(jQuery);
*/