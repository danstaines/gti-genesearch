/*
 * Copyright [1999-2021] EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var examples = {
	human : '{"genome":"homo_sapiens"}',
	e_coli : '{"genome":"escherichia_coli_str_k_12_substr_mg1655"}',
	eschericia_lacZ : '{"lineage":"561","name":"lacZ"}',
	escherichia_signals : '{"lineage":"561","GO":"GO:0035556"}',
	wheat_hypervirulence : '{"annotations":{"host":"4565","phenotype":"PHI:1000008"}}',
	ascomycota_hydrolase : '{"lineage":"4890","GO":"GO:0016787"}',
	mammal_brca2_homologues : '{"lineage":"40674","Pfam":"PF09121","homologues":{"stable_id":"ENSG00000139618"}}',
	human_chr1 : '{"genome":"homo_sapiens","location":{"seq_region_name":"1","start":"45000","end":"96000"}}',
	uniprots : '{"Uniprot_SWISSPROT":["P03886","P03891","P00395","P00403"]}'
};

var setQueryExample = function(exampleName) {
	console.log("Setting example " + exampleName)
	$('#query').val(examples[exampleName]);
};

var allFields;
var fields;
var facetFields;
var advancedSearch = false;

/**
 * Button to switch between advanced and simple search
 */
$('.btn-toggle').click(function() {

	$(this).find('.btn').toggleClass('active');

	if ($(this).find('.btn-primary').size() > 0) {
		$(this).find('.btn').toggleClass('btn-primary');
	}

	$(this).find('.btn').toggleClass('btn-default');

	advancedSearch = !advancedSearch;
	if (advancedSearch) {
		$('#adv_query').show();
		$('#set_examples').show();
		$('#simple_query').hide();
	} else {
		$('#adv_query').hide();
		$('#set_examples').hide();
		$('#simple_query').show();
	}
});

/**
 * Populate the base form
 */
$(document).ready(
		function() {
			$('#search').hide();
			$('#adv_query').hide();
			$('#set_examples').hide();
			$.get("/api/fieldinfo", function(data) {
				allFields = {};
				fields = [];
				facetFields = [];
				data.forEach(function(field) {
					allFields[field.name] = field;
					fields.push({
						id : field.name,
						text : field.displayName
					});
					if (field.facet) {
						facetFields.push({
							id : field.name,
							text : field.displayName
						});
					}
				});
				console.log("Fields loaded");
				// populate the fields selector
				console.trace(allFields);
				$('#fields').select2({
					multiple : "multiple",
					data : fields,
					width : "auto",
					dropdownAutoWidth : true
				});
				// maintain order of addition - new selections are appended
				$("select").on("select2:select", function(evt) {
					var element = evt.params.data.element;
					var $element = $(element);

					$element.detach();
					$(this).append($element);
					$(this).trigger("change");
				});
				// set some defaults (reverse order)
				$("#fields").val([ "description", "name", "genome", "id" ])
						.trigger("change");

				$('#facets').select2({
					multiple : "multiple",
					data : facetFields,
					width : "auto",
					dropdownAutoWidth : true,
					placeholder : "(Optional) add a facet"
				});

				$('#query_field').select2({
					data : fields,
					width : "auto",
					dropdownAutoWidth : true
				});
				$('#query_field').trigger('change');
				$('#search').show();
			});
		});

/**
 * For an ontology input, set up to use OLS autocomplete
 * 
 * @param element
 * @param ontology
 *            name of ontology in OLS
 */
function setOntologyComplete(element, ontology) {
	element.autocomplete({
		minLength : 3,
		source : function(request, response) {
			$.get("http://www.ebi.ac.uk/ols/api/select?ontology=" + ontology
					+ "&q=" + request.term, function(data) {
				var array = data.error ? [] : $.map(data.response.docs,
						function(m) {
							var label = m.label + " [" + m.obo_id + "]";
							var value = m.obo_id;
							return {
								label : label,
								value : value.replace("NCBITaxon:","")
							};
						});
				response(array);
			});
			return;
		},
		select : function(event, ui) {
			var auto = $(event.target);
			console.info("selected ontology " + ui.item.label);
			auto.val(ui.item.label);
			auto.siblings('.values').first().val(ui.item.value);
			return false;
		}
	});
}

/**
 * For a genome element, set up autocomplete
 * 
 * @param element
 */
function setGenomeComplete(element) {
	element.autocomplete({
		minLength : 3,
		source : function(request, response) {
			$.get("/api/genomes/select?query=" + request.term, function(data) {
				var array = data.error ? [] : $.map(data.results, function(m) {
					var labelStr = m.organism.display_name;
					if (m.organism.display_name != m.organism.scientific_name) {
						labelStr = labelStr + " ("
								+ m.organism.scientific_name + ")";
					}
					return {
						label : labelStr,
						value : m.id
					};
				});
				response(array);
			});
			return;
		},
		select : function(event, ui) {
			var auto = $(event.target);
			console.info("selected genome " + ui.item.label);
			auto.val(ui.item.label);
			auto.siblings('.values').first().val(ui.item.value);
			return false;
		}
	});
}

/**
 * Dynamically set correct type for a new field
 * @param element
 */
function setQueryInput(element) {
	console.info("Setting field input");
	var sib = element.parent().siblings('.query_value').first();
	var select;
	switch (element.val()) {
	case "genome":
		select = $('#genomeTemplate').clone().removeClass('hide').removeAttr(
				'id');
		setGenomeComplete(select.children('.genome').first());
		break;
	case "lineage":
		select = $('#lineageTemplate').clone().removeClass('hide').removeAttr(
				'id');
		setOntologyComplete(select.children('.lineage').first(), "ncbitaxon");
		break;
	case "GO":
		select = $('#goTemplate').clone().removeClass('hide').removeAttr('id');
		setOntologyComplete(select.children('.go').first(), "go");
		break;
	case "id":
		select = $('#idTemplate').clone().removeClass('hide').removeAttr('id');
		break;
	default:
		select = $('#textTemplate').clone().removeClass('hide')
				.removeAttr('id');
		break;
	}
	sib.replaceWith(select);
}

/*
 * When selecting a new query field, trigger the input type
 */
$('.query_field').change(function(e) {
	setQueryInput($(this));
});

/*
 * Add functionality to field add button
 */
$('#add_button').click(
		function(e) { // on add input button click
			e.preventDefault();
			var template = $('#query_template');
			var clone = template.clone().removeClass('hide').removeAttr('id')
					.insertBefore(template);
			var querySelector = clone.find('.query_field');
			querySelector.select2({
				data : fields,
				width : "auto",
				dropdownAutoWidth : true
			});
			querySelector.change(function(e) {
				setQueryInput($(this));
			});
			querySelector.trigger('change');
			clone.find(".remove_button").click(function() {
				$(this).parent().parent().remove();
			});
		});

var table;
/**
 * Submit the search with supplied values
 * 
 * @param search
 *            hash of queries, facets and fields
 */
function submitSearch(search) {

	// only invoke if rows==0 ie. all query rows processed
	if (rows > 0) {
		console.info("Not submitting search - " + rows + " rows remain");
		return;
	}

	console.info("Submitting search");
	console.trace(search);

	var n = 0;
	var columns = [];
	search.fields.forEach(function(column) {
		console.log("Creating field " + column.name);
		columns.push({
			data : column.displayField,
			title : column.displayName,
			type : 'string',
			visible : true,
			sortable : true,
			targets : n++
		});
	});

	var options = {
		processing : true,
		serverSide : true,
		pagingType : 'simple',
		order : [],
		searching : false,
		ajax : {
			url : '/api/genes/query',
			type : 'POST',
			contentType : 'application/json',
			data : function(data) {
				console.log("Posting data");
				// setting sort
				var sorts = [];
				for (var i = 0; i < data.order.length; i++) {
					var field = search.fields[data.order[i].column];
					var sort = field.searchField;
					if (data.order[i].dir == 'desc') {
						sort = '-' + sort;
					}
					console.log("Sorting on " + sort);
					sorts.push(sort);
				}
				console.trace(JSON.stringify(data));
				// create post
				return JSON.stringify({
					query : search.query,
					fields : map(search.fields, function(f) {
						return f.displayField
					}),
					facets : map(search.facets, function(f) {
						return f.searchField
					}),
					sort : sorts,
					offset : data.start,
					limit : data.length
				});
			},
			dataFilter : function(json) {
				console.log("Filtering data");
				response = JSON.parse(json);
				// modify output
				response.recordsTotal = response.resultCount;
				response.recordsFiltered = response.resultCount;
				response.data = response.results;
				response.data.forEach(function(row) {
					search.fields.forEach(function (field) {
						if(!row[field.displayField]) {
							row[field.displayField] = "";
						}
					});
				});
				response.results = undefined;
				console.log("Completed filtering data");
				console.trace(response);
				return JSON.stringify(response);
			}
		},
		columnDefs : columns
	};
	console.trace(options);
	if (table) {
		console.log("Destroying table");
		table.destroy();
		$('#results').empty();
	}
	console.log("Creating table");
	table = $('#results').DataTable(options);
	console.log("Created table");
	return;
}

$('#searchButton').click(function() {
	processQueryForm(submitSearch, {});
});

$('#xmlButton').click(function() {
	processQueryForm(downloadData, {
		format : 'application/xml'
	});
});
$('#jsonButton').click(function() {
	processQueryForm(downloadData, {
		format : 'application/json'
	});
});
$('#csvButton').click(function() {
	processQueryForm(downloadData, {
		format : 'text/csv'
	});
});

/**
 * Method to invoke the endpoint to download data to a file
 * 
 * @param format
 *            desired format eg csv, json, xml
 */
function downloadData(search) {
	// console.trace(search);
	var postData = {
		query : search.query,
		fields : map(search.fields, function(f) {
			return f.displayField
		}),
		accept : search.format
	};

	// Build a temporary hidden form
	var form = $('<form></form>').attr('action', '/api/genes/fetch').attr(
			'method', 'post');

	$('<input>').attr({
		type : 'hidden',
		name : 'query',
		value : JSON.stringify(search.query)
	}).appendTo(form);

	$('<input>').attr({
		type : 'hidden',
		name : 'accept',
		value : search.format
	}).appendTo(form);

	$('<input>').attr({
		type : 'hidden',
		name : 'fields',
		value : map(search.fields, function(f) {
			return f.displayField
		})
	}).appendTo(form);

	form.appendTo('body').submit().remove();
	return;

}

var rows = 0;

/**
 * Process the form, and call the supplied callback after each query row is
 * processed Uses rows to make sure each row has been processed, to allow for
 * async method calls
 * 
 * @param callback
 *            function to invoke after each row. e.g. will submit the query if
 *            no outstanding rows
 */
function processQueryForm(callback, search) {

	if (!search) {
		search = {};
	}
	search.fields = [];
	search.facets = [];

	var fieldsV = $('#fields').val();
	if (fieldsV) {
		fieldsV.forEach(function(field) {
			console.log(field);
			var f = allFields[field];
			console.trace(f);
			search.fields.push(f);
		});
	}
	var facetsV = $('#facets').val();
	if (facetsV) {
		facetsV.forEach(function(field) {
			console.log(field);
			var f = allFields[field];
			console.trace(f);
			search.facets.push(f);
		});
	}

	if (advancedSearch) {
		search.query = JSON.parse($('#query').val());
		callback(search);
	} else {
		search.query = {};
		rows = 0;
		// find all simple query fields and collapse together
		$('#simple_query').find('.query_row').not('.hide').each(
				function(i, row) {
					processRow($(this), search, callback);
				});
	}

	return;

};

/*
 * Function to process a row in the query form callback is a function to call
 * after each row has been processed
 */
function processRow(row, search, callback) {
	console.info("Parsing query row");
	console.trace(row);
	var field = row.find('.query_field').val();
	var inputs = row.find('.values');
	console.info("Adding " + inputs.length + " rows");
	rows += inputs.length;
	inputs.each(function() {
		console.info("Processing" + $(this));
		if ($(this).is("textarea")) {
			setQueryVal(search, field, $(this).val().split("\n"));
			callback(search);
		} else {
			if ($(this).attr('type') == 'file') {
				var files = $(this).get(0).files;
				if (files && files.length > 0) {
					var file = files[0];
					var reader = new FileReader();
					reader.onloadend = function(evt) {
						// file is loaded
						console.info("Read completed");
						result = evt.target.result;
						val = result.split("\n");
						console.info("Setting value");
						setQueryVal(search, field, val);
						callback(search);
					};
					console.info("Starting read");
					reader.readAsText(file);
				} else {
					console.info("No files to read");
					rows--;
					callback(search);
				}
			} else {
				setQueryVal(search, field, $(this).val());
				callback(search);
			}
		}
	})
}

/*
 * Set supplied key/value in a query
 */
function setQueryVal(search, field, val) {
	console.info("Setting search for " + field + "/" + val);
	if (val) {

		// filter out empty strings
		if (val instanceof Array) {
			val = val.filter(function(e) {
				return e && e.length > 0;
			});
		}

		if (search.query[field]) {
			if (search.query[field] instanceof Array) {
				if (val instanceof Array) {
					search.query[field] = search.query[field].concat(val);
				} else {
					search.query[field].push(val);
				}
			} else if (val.length > 0) {
				if (val instanceof Array) {
					search.query.concat(val);
				} else {
					search.query[field] = [ search.query[field], val ];
				}
			}
		} else {
			search.query[field] = val;
		}
	}
	console.trace();
	console.info("Finished " + field + " - row " + rows);
	rows--;
	return;
}

/**
 * Function to process one array and return another using a callback
 * 
 * @param objs
 * @param callback
 * @returns {Array}
 */
function map(objs, callback) {
	var results = [];
	objs.forEach(function(obj) {
		results.push(callback(obj));
	});
	return results;
}
