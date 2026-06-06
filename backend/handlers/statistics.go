package handlers

import (
	"net/http"

	"ledger-origin/config"
	"ledger-origin/middleware"

	"github.com/gin-gonic/gin"
)

type StatItem struct {
	CategoryName string  `db:"category_name" json:"category_name"`
	CategoryIcon string  `db:"category_icon" json:"category_icon"`
	Amount       float64 `db:"amount" json:"amount"`
	Count        int     `db:"count" json:"count"`
}

type DailyStat struct {
	Date    string  `db:"date" json:"date"`
	Income  float64 `db:"income" json:"income"`
	Expense float64 `db:"expense" json:"expense"`
}

func GetStatOverview(c *gin.Context) {
	userID := middleware.GetUserID(c)
	month := c.Query("month") // 2024-01

	var totalIncome, totalExpense float64
	config.Session.Select("COALESCE(SUM(amount), 0)").From("transactions").
		Where("user_id = ? AND type = 2 AND DATE_FORMAT(date, '%Y-%m') = ?", userID, month).
		LoadOne(&totalIncome)
	config.Session.Select("COALESCE(SUM(amount), 0)").From("transactions").
		Where("user_id = ? AND type = 1 AND DATE_FORMAT(date, '%Y-%m') = ?", userID, month).
		LoadOne(&totalExpense)

	c.JSON(http.StatusOK, gin.H{"code": 200, "data": gin.H{
		"income":  totalIncome,
		"expense": totalExpense,
		"balance": totalIncome - totalExpense,
	}})
}

func GetStatByCategory(c *gin.Context) {
	userID := middleware.GetUserID(c)
	month := c.Query("month")
	txType := c.DefaultQuery("type", "1")

	var items []StatItem
	config.Session.Select(
		"c.name as category_name, c.icon as category_icon, SUM(t.amount) as amount, COUNT(*) as count",
	).From("transactions t").
		LeftJoin("categories c", "t.category_id = c.id").
		Where("t.user_id = ? AND t.type = ? AND DATE_FORMAT(t.date, '%Y-%m') = ?", userID, txType, month).
		GroupBy("t.category_id").
		OrderDir("amount", false).
		Load(&items)

	c.JSON(http.StatusOK, gin.H{"code": 200, "data": items})
}

func GetStatDaily(c *gin.Context) {
	userID := middleware.GetUserID(c)
	month := c.Query("month")

	var stats []DailyStat
	config.Session.SelectBySql(`
		SELECT date,
			SUM(CASE WHEN type = 2 THEN amount ELSE 0 END) as income,
			SUM(CASE WHEN type = 1 THEN amount ELSE 0 END) as expense
		FROM transactions
		WHERE user_id = ? AND DATE_FORMAT(date, '%Y-%m') = ?
		GROUP BY date ORDER BY date
	`, userID, month).Load(&stats)

	c.JSON(http.StatusOK, gin.H{"code": 200, "data": stats})
}
