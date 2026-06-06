package models

import "time"

type Budget struct {
	ID         int64     `db:"id" json:"id"`
	UserID     int64     `db:"user_id" json:"user_id"`
	CategoryID int64     `db:"category_id" json:"category_id"`
	Amount     float64   `db:"amount" json:"amount"`
	Month      string    `db:"month" json:"month"` // 2024-01 格式
	CreatedAt  time.Time `db:"created_at" json:"created_at"`
	UpdatedAt  time.Time `db:"updated_at" json:"updated_at"`
}

type BudgetReq struct {
	CategoryID int64   `json:"category_id" binding:"required"`
	Amount     float64 `json:"amount" binding:"required,gt=0"`
	Month      string  `json:"month" binding:"required"`
}

type BudgetResp struct {
	Budget
	CategoryName string  `json:"category_name"`
	Spent        float64 `json:"spent"`
	Remaining    float64 `json:"remaining"`
	Percentage   float64 `json:"percentage"`
}
