package com.reportolia.core.model.variable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.reportolia.core.Constants;
import com.reportolia.core.model.base.BaseEntity;
import com.reportolia.core.model.datatype.DataType;
import com.reportolia.core.model.table.DbTableColumn;

/**
 * 
 * The Variable class
 *
 * @author Batir Akhmerov
 * Created on Nov 13, 2015
 */
@Entity
@Table(name="r3p_variables", uniqueConstraints = { @UniqueConstraint(columnNames = {"owner_type", "owner_id", "name"}) })
public class Variable extends BaseEntity {
    
	@Enumerated(EnumType.STRING)
	@Column(name = "owner_type", nullable = false, length = Constants.LENGTH_OWNER_TYPE)
    private VariableOwnerType ownerType;
	
    @Column(name = "owner_id")
    private Long ownerId;
    
    @Enumerated(EnumType.STRING)
	@Column(name = "data_type", nullable = false, length = Constants.LENGTH_DATA_TYPE)
    private DataType dataType;
    
    /**
     * Not-correlated calculated column used as value source to select variable values from
     */
    @ManyToOne
    @JoinColumn(name="value_source_column_id", nullable=true)
    private DbTableColumn valueSourceDbColumn;
    
    
    @Column(name = "name", nullable = false, length = 128)
    private String name;


	public VariableOwnerType getOwnerType() {
		return this.ownerType;
	}

	public void setOwnerType(VariableOwnerType ownerType) {
		this.ownerType = ownerType;
	}


	public Long getOwnerId() {
		return this.ownerId;
	}


	public void setOwnerId(Long ownerId) {
		this.ownerId = ownerId;
	}


	public DataType getDataType() {
		return this.dataType;
	}


	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}


	public DbTableColumn getValueSourceDbColumn() {
		return this.valueSourceDbColumn;
	}


	public void setValueSourceDbColumn(DbTableColumn valueSourceDbColumn) {
		this.valueSourceDbColumn = valueSourceDbColumn;
	}


	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
    
    
	
}
