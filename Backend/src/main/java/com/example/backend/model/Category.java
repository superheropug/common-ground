package com.example.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Only contains a String which is the category name (not allowed to change, PK) and a String which is the fancy category name. */
@Entity
@Table(name = "CATEGORIES")
public class Category {

	@Id
	@Column(name = "NAME", nullable = false, updatable = false)
	private String name;

	@Column(name = "FANCY_NAME")
	private String fancyName;

	protected Category() {
	}

	public Category(String name, String fancyName) {
		this.name = name;
		this.fancyName = fancyName;
	}

	public String getName() {
		return name;
	}

	public String getFancyName() {
		return fancyName;
	}

	public void setFancyName(String fancyName) {
		this.fancyName = fancyName;
	}
}
