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

	items := make([]StatItem, 0)
	sql := `SELECT COALESCE(c.name, '') AS category_name,
			COALESCE(c.icon, '') AS category_icon,
			SUM(t.amount) AS amount, COUNT(*) AS count
		FROM transactions t
		LEFT JOIN categories c ON t.category_id = c.id
		WHERE t.user_id = ? AND t.type = ? AND DATE_FORMAT(t.date, '%Y-%m') = ?
		GROUP BY t.category_id, c.name, c.icon
		ORDER BY amount DESC`
	_, err := config.Session.SelectBySql(sql, userID, txType, month).Load(&items)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"code": 500, "msg": "统计失败: " + err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"code": 200, "data": items})
}

func GetStatDaily(c *gin.Context) {
	userID := middleware.GetUserID(c)
	month := c.Query("month")

	stats := make([]DailyStat, 0)
	_, err := config.Session.SelectBySql(`
		SELECT DATE_FORMAT(date, '%Y-%m-%d') AS date,
			SUM(CASE WHEN type = 2 THEN amount ELSE 0 END) as income,
			SUM(CASE WHEN type = 1 THEN amount ELSE 0 END) as expense
		FROM transactions
		WHERE user_id = ? AND DATE_FORMAT(date, '%Y-%m') = ?
		GROUP BY DATE_FORMAT(date, '%Y-%m-%d') ORDER BY date
	`, userID, month).Load(&stats)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"code": 500, "msg": "统计失败: " + err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"code": 200, "data": stats})
}
