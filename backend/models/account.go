package models

import "time"

type Account struct {
	ID        int64     `db:"id" json:"id"`
	UserID    int64     `db:"user_id" json:"user_id"`
	Name      string    `db:"name" json:"name"`
	Type      string    `db:"type" json:"type"` // cash, bank, alipay, wechat, credit
	Balance   float64   `db:"balance" json:"balance"`
	Icon      string    `db:"icon" json:"icon"`
	CreatedAt time.Time `db:"created_at" json:"created_at"`
	UpdatedAt time.Time `db:"updated_at" json:"updated_at"`
}

type AccountReq struct {
	Name    string  `json:"name" binding:"required"`
	Type    string  `json:"type" binding:"required"`
	Balance float64 `json:"balance"`
	Icon    string  `json:"icon"`
}
