package models

type Transaction struct {
	ID         int64   `db:"id" json:"id"`
	UserID     int64   `db:"user_id" json:"user_id"`
	Type       int     `db:"type" json:"type"` // 1=支出 2=收入
	Amount     float64 `db:"amount" json:"amount"`
	CategoryID int64   `db:"category_id" json:"category_id"`
	AccountID  int64   `db:"account_id" json:"account_id"`
	Note       string  `db:"note" json:"note"`
	Date       string  `db:"date" json:"date"`
	CreatedAt  string  `db:"created_at" json:"created_at"`
}

type TransactionReq struct {
	Type       int     `json:"type" binding:"required,oneof=1 2"`
	Amount     float64 `json:"amount" binding:"required,gt=0"`
	CategoryID int64   `json:"category_id" binding:"required"`
	AccountID  int64   `json:"account_id" binding:"required"`
	Note       string  `json:"note"`
	Date       string  `json:"date" binding:"required"`
}

type TransactionResp struct {
	ID           int64   `db:"id" json:"id"`
	UserID       int64   `db:"user_id" json:"user_id"`
	Type         int     `db:"type" json:"type"`
	Amount       float64 `db:"amount" json:"amount"`
	CategoryID   int64   `db:"category_id" json:"category_id"`
	AccountID    int64   `db:"account_id" json:"account_id"`
	Note         string  `db:"note" json:"note"`
	Date         string  `db:"date" json:"date"`
	CreatedAt    string  `db:"created_at" json:"created_at"`
	CategoryName string  `db:"category_name" json:"category_name"`
	CategoryIcon string  `db:"category_icon" json:"category_icon"`
	AccountName  string  `db:"account_name" json:"account_name"`
}
