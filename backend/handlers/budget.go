package handlers

import (
	"net/http"

	"ledger-origin/config"
	"ledger-origin/middleware"
	"ledger-origin/models"

	"github.com/gin-gonic/gin"
)

func GetBudgets(c *gin.Context) {
	userID := middleware.GetUserID(c)
	month := c.Query("month")

	budgets := make([]models.BudgetResp, 0)
	sql := `SELECT b.id, b.user_id, b.category_id, b.amount, b.month,
			COALESCE(c.name, '') AS category_name
		FROM budgets b
		LEFT JOIN categories c ON b.category_id = c.id
		WHERE b.user_id = ?`
	args := []interface{}{userID}
	if month != "" {
		sql += " AND b.month = ?"
		args = append(args, month)
	}
	if _, err := config.Session.SelectBySql(sql, args...).Load(&budgets); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"code": 500, "msg": "查询失败: " + err.Error()})
		return
	}

	// 计算各预算的已用金额
	for i, b := range budgets {
		var spent float64
		config.Session.Select("COALESCE(SUM(amount), 0)").From("transactions").
			Where("user_id = ? AND category_id = ? AND type = 1 AND DATE_FORMAT(date, '%Y-%m') = ?",
				userID, b.CategoryID, b.Month).LoadOne(&spent)
		budgets[i].Spent = spent
		budgets[i].Remaining = b.Amount - spent
		if b.Amount > 0 {
			budgets[i].Percentage = spent / b.Amount * 100
		}
	}

	c.JSON(http.StatusOK, gin.H{"code": 200, "data": budgets})
}

func CreateBudget(c *gin.Context) {
	userID := middleware.GetUserID(c)
	var req models.BudgetReq
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"code": 400, "msg": "参数错误"})
		return
	}

	// 检查是否已存在相同分类的预算
	var count int
	config.Session.Select("COUNT(*)").From("budgets").
		Where("user_id = ? AND category_id = ? AND month = ?", userID, req.CategoryID, req.Month).
		LoadOne(&count)
	if count > 0 {
		c.JSON(http.StatusBadRequest, gin.H{"code": 400, "msg": "该分类本月已设置预算"})
		return
	}

	result, err := config.Session.InsertInto("budgets").
		Columns("user_id", "category_id", "amount", "month").
		Values(userID, req.CategoryID, req.Amount, req.Month).
		Exec()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"code": 500, "msg": "添加失败"})
		return
	}

	id, _ := result.LastInsertId()
	c.JSON(http.StatusOK, gin.H{"code": 200, "msg": "添加成功", "data": gin.H{"id": id}})
}

func UpdateBudget(c *gin.Context) {
	userID := middleware.GetUserID(c)
	id := c.Param("id")
	var req models.BudgetReq
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"code": 400, "msg": "参数错误"})
		return
	}

	_, err := config.Session.Update("budgets").
		Set("category_id", req.CategoryID).
		Set("amount", req.Amount).
		Set("month", req.Month).
		Where("id = ? AND user_id = ?", id, userID).Exec()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"code": 500, "msg": "更新失败"})
		return
	}
	c.JSON(http.StatusOK, gin.H{"code": 200, "msg": "更新成功"})
}

func DeleteBudget(c *gin.Context) {
	userID := middleware.GetUserID(c)
	id := c.Param("id")

	_, err := config.Session.DeleteFrom("budgets").
		Where("id = ? AND user_id = ?", id, userID).Exec()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"code": 500, "msg": "删除失败"})
		return
	}
	c.JSON(http.StatusOK, gin.H{"code": 200, "msg": "删除成功"})
}
