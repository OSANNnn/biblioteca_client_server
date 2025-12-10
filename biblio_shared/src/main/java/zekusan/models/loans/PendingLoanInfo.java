package zekusan.models.loans;

import java.time.LocalDate;

import zekusan.enums.ItemType;

public class PendingLoanInfo {
	private int id;
	private int itemId;
	private String itemName;
	private ItemType category;
	private String borrower;
	private LocalDate requestedOn;

	public PendingLoanInfo() {
	}

	public PendingLoanInfo(int id, int itemId, String itemName, ItemType category, String borrower,
			LocalDate requestedOn) {
		this.id = id;
		this.itemId = itemId;
		this.itemName = itemName;
		this.category = category;
		this.borrower = borrower;
		this.requestedOn = requestedOn;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public ItemType getCategory() {
		return category;
	}

	public void setCategory(ItemType category) {
		this.category = category;
	}

	public String getBorrower() {
		return borrower;
	}

	public void setBorrower(String borrower) {
		this.borrower = borrower;
	}

	public LocalDate getRequestedOn() {
		return requestedOn;
	}

	public void setRequestedOn(LocalDate requestedOn) {
		this.requestedOn = requestedOn;
	}
}
