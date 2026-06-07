package handlers

import (
	"net/http"
	"strconv"

	"ledger-origin/config"
	"ledger-origin/middleware"
	"ledger-origin/models"

	"github.com/gin-gonic/gin"
	"github.com/gocraft/dbr/v2"
)

func CreateTransaction(c *gin.Context) {
	userID := middleware.GetUserID(c)
	var req models.TransactionReq
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"code": 400, "msg": "参数错误: " + err.Error()})
		return
	}

	result, err := config.Session.InsertInto("transactions").
		Columns("user_id", "type", "amount", "category_id", "account_id", "note", "date").
		Values(userID, req.Type, req.Amount, req.CategoryID, req.AccountID, req.Note, req.Date).
		Exec()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"code": 500, "msg": "添加失败"})
		return
	}

	// 更新账户余额
	if req.Type == 1 { // 支出
		config.Session.Update("accounts").Set("balance", dbr.Expr("balance - ?", req.Amount)).
			Where("id = ? AND user_id = ?", req.AccountID, userID).Exec()
	} else { // 收入
		config.Session.Update("accounts").Set("balance", dbr.Expr("balance + ?", req.Amount)).
			Where("id = ? AND user_id = ?", req.AccountID, userID).Exec()
	}

	id, _ := result.LastInsertId()
	c.JSON(http.StatusOK, gin.H{"code": 200, "msg": "添加成功", "data": gin.H{"id": id}})
}

func GetTransactions(c *gin.Context) {
	userID := middleware.GetUserID(c)
	page, _ := strconv.Atoi(c.DefaultQuery("page", "1"))
	pageSize, _ := strconv.Atoi(c.DefaultQuery("page_size", "20"))
	dateFrom := c.Query("date_from")
	dateTo := c.Query("date_to")
	txType := c.Query("type")
	categoryID := c.Query("category_id")

	offset := (page - 1) * pageSize

	query := config.Session.Select(
		"t.id", "t.user_id", "t.type", "t.amount", "t.category_id", "t.account_id",
		"t.note",
		"DATE_FORMAT(t.date, '%Y-%m-%d') AS date",
		"DATE_FORMAT(t.created_at, '%Y-%m-%d %H:%i:%s') AS created_at",
		"COALESCE(c.name, '') AS category_name",
		"COALESCE(c.icon, '') AS category_icon",
		"COALESCE(a.name, '') AS account_name",
	).From("transactions t").
		LeftJoin("categories c", "t.category_id = c.id").
		LeftJoin("accounts a", "t.account_id = a.id").
		Where("t.user_id = ?", userID)

	if dateFrom != "" {
		query = query.Where("t.date >= ?", dateFrom)
	}
	if dateTo != "" {
		query = query.Where("t.date <= ?", dateTo)
	}
	if txType != "" {
		query = query.Where("t.type = ?", txType)
	}
	if categoryID != "" {
		query = query.Where("t.category_id = ?", categoryID)
	}

	transactions := make([]models.TransactionResp, 0)
	if _, err := query.OrderDir("t.date", false).OrderDir("t.id", false).
		Limit(uint64(pageSize)).Offset(uint64(offset)).Load(&transactions); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"code": 500, "msg": "查询失败: " + err.Error()})
		return
	}

	// 获取总数
	var total int
	countQuery := config.Session.Select("COUNT(*)").From("transactions t").
		Where("t.user_id = ?", userID)
	if dateFrom != "" {
		countQuery = countQuery.Where("t.date >= ?", dateFrom)
	}
	if dateTo != "" {
		countQuery = countQuery.Where("t.date <= ?", dateTo)
	}
	if txType != "" {
		countQuery = countQuery.Where("t.type = ?", txType)
	}
	countQuery.LoadOne(&total)

	c.JSON(http.StatusOK, gin.H{"code": 200, "data": gin.H{
		"list":  transactions,
		"total": total,
		"page":  page,
	}})
}

func UpdateTransaction(c *gin.Context) {
	userID := middleware.GetUserID(c)
	id := c.Param("id")
	var req models.TransactionReq
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"code": 400, "msg": "参数错误"})
		return
	}

	_, err := config.Session.Update("transactions").
		Set("type", req.Type).
		Set("amount", req.Amount).
		Set("category_id", req.CategoryID).
		Set("account_id", req.AccountID).
		Set("note", req.Note).
		Set("date", req.Date).
		Where("id = ? AND user_id = ?", id, userID).Exec()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"code": 500, "msg": "更新失败"})
		return
	}
	c.JSON(http.StatusOK, gin.H{"code": 200, "msg": "更新成功"})
}

func DeleteTransaction(c *gin.Context) {
	userID := middleware.GetUserID(c)
	id := c.Param("id")

	_, err := config.Session.DeleteFrom("transactions").
		Where("id = ? AND user_id = ?", id, userID).Exec()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"code": 500, "msg": "删除失败"})
		return
	}
	c.JSON(http.StatusOK, gin.H{"code": 200, "msg": "删除成功"})
}
