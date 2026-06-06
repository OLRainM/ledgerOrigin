package models

type Category struct {
	ID        int64  `db:"id" json:"id"`
	UserID    int64  `db:"user_id" json:"user_id"`
	Name      string `db:"name" json:"name"`
	Icon      string `db:"icon" json:"icon"`
	Type      int    `db:"type" json:"type"` // 1=支出 2=收入
	SortOrder int    `db:"sort_order" json:"sort_order"`
	CreatedAt string `db:"created_at" json:"created_at"`
}

type CategoryReq struct {
	Name      string `json:"name" binding:"required"`
	Icon      string `json:"icon"`
	Type      int    `json:"type" binding:"required,oneof=1 2"`
	SortOrder int    `json:"sort_order"`
}
