package handlers

import (
	"net/http"

	"ledger-origin/config"
	"ledger-origin/middleware"
	"ledger-origin/models"

	"github.com/gin-gonic/gin"
)

func GetCategories(c *gin.Context) {
	userID := middleware.GetUserID(c)
	txType := c.Query("type")

	query := config.Session.Select("*").From("categories").
		Where("user_id = ?", userID)
	if txType != "" {
		query = query.Where("type = ?", txType)
	}

	var categories []models.Category
	query.OrderAsc("sort_order").Load(&categories)

	c.JSON(http.StatusOK, gin.H{"code": 200, "data": categories})
}

func CreateCategory(c *gin.Context) {
	userID := middleware.GetUserID(c)
	var req models.CategoryReq
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"code": 400, "msg": "参数错误"})
		return
	}

	result, err := config.Session.InsertInto("categories").
		Columns("user_id", "name", "icon", "type", "sort_order").
		Values(userID, req.Name, req.Icon, req.Type, req.SortOrder).
		Exec()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"code": 500, "msg": "添加失败"})
		return
	}

	id, _ := result.LastInsertId()
	c.JSON(http.StatusOK, gin.H{"code": 200, "msg": "添加成功", "data": gin.H{"id": id}})
}

func UpdateCategory(c *gin.Context) {
	userID := middleware.GetUserID(c)
	id := c.Param("id")
	var req models.CategoryReq
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"code": 400, "msg": "参数错误"})
		return
	}

	_, err := config.Session.Update("categories").
		Set("name", req.Name).
		Set("icon", req.Icon).
		Set("type", req.Type).
		Set("sort_order", req.SortOrder).
		Where("id = ? AND user_id = ?", id, userID).Exec()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"code": 500, "msg": "更新失败"})
		return
	}
	c.JSON(http.StatusOK, gin.H{"code": 200, "msg": "更新成功"})
}

func DeleteCategory(c *gin.Context) {
	userID := middleware.GetUserID(c)
	id := c.Param("id")

	_, err := config.Session.DeleteFrom("categories").
		Where("id = ? AND user_id = ?", id, userID).Exec()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"code": 500, "msg": "删除失败"})
		return
	}
	c.JSON(http.StatusOK, gin.H{"code": 200, "msg": "删除成功"})
}
