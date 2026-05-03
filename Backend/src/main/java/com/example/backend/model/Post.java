package com.example.backend.model;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

/** A Post has a Text field, (NOT varchar256, NEEDS to be Text) containing the Text of the message. It contains a Username field binding it to the User.java. A repository will exist to find posts by a given username. The primary key is a unique ID that is also used to request a specific post. Also contains a List of Categories maintained by some table? its a many-to-many relation.*/
@Entity
@Table(name = "POSTS")
public class Post {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "text", nullable = false)
	private String text;

	@Column(name = "username", nullable = false)
	private String username;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt = Instant.now();

	@Column(name = "post_time", nullable = false)
	private Instant postTime = Instant.now();

	    @ManyToMany(fetch = FetchType.LAZY)
	    @JoinTable(
		    name = "POSTS_CATEGORIES",
		    joinColumns = @JoinColumn(name = "post_id"),
		    inverseJoinColumns = @JoinColumn(name = "category_name")
	    )
	    private Set<Category> categories = new HashSet<>();

	public Post() {}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getPostTime() {
		return postTime;
	}

	public void setPostTime(Instant postTime) {
		this.postTime = postTime;
	}

	public Set<Category> getCategories() {
		return categories;
	}

	public void setCategories(Set<Category> categories) {
		this.categories = categories;
	}

	public void addCategory(Category category) {
		this.categories.add(category);
	}

	public void removeCategory(Category category) {
		this.categories.remove(category);
	}
}
