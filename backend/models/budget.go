package models

type Budget struct {
	ID         int64   `db:"id" json:"id"`
	UserID     int64   `db:"user_id" json:"user_id"`
	CategoryID int64   `db:"category_id" json:"category_id"`
	Amount     float64 `db:"amount" json:"amount"`
	Month      string  `db:"month" json:"month"` // 2024-01 格式
	CreatedAt  string  `db:"created_at" json:"created_at"`
	UpdatedAt  string  `db:"updated_at" json:"updated_at"`
}

type BudgetReq struct {
	CategoryID int64   `json:"category_id" binding:"required"`
	Amount     float64 `json:"amount" binding:"required,gt=0"`
	Month      string  `json:"month" binding:"required"`
}

type BudgetResp struct {
	ID           int64   `db:"id" json:"id"`
	UserID       int64   `db:"user_id" json:"user_id"`
	CategoryID   int64   `db:"category_id" json:"category_id"`
	Amount       float64 `db:"amount" json:"amount"`
	Month        string  `db:"month" json:"month"`
	CategoryName string  `db:"category_name" json:"category_name"`
	Spent        float64 `json:"spent"`
	Remaining    float64 `json:"remaining"`
	Percentage   float64 `json:"percentage"`
}
