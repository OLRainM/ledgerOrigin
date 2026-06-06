package handlers

import (
	"net/http"

	"ledger-origin/config"
	"ledger-origin/middleware"
	"ledger-origin/models"

	"github.com/gin-gonic/gin"
)

func GetAccounts(c *gin.Context) {
	userID := middleware.GetUserID(c)
	var accounts []models.Account
	config.Session.Select("*").From("accounts").
		Where("user_id = ?", userID).OrderAsc("id").Load(&accounts)

	c.JSON(http.StatusOK, gin.H{"code": 200, "data": accounts})
}

func CreateAccount(c *gin.Context) {
	userID := middleware.GetUserID(c)
	var req models.AccountReq
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"code": 400, "msg": "参数错误"})
		return
	}

	result, err := config.Session.InsertInto("accounts").
		Columns("user_id", "name", "type", "balance", "icon").
		Values(userID, req.Name, req.Type, req.Balance, req.Icon).
		Exec()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"code": 500, "msg": "添加失败"})
		return
	}

	id, _ := result.LastInsertId()
	c.JSON(http.StatusOK, gin.H{"code": 200, "msg": "添加成功", "data": gin.H{"id": id}})
}

func UpdateAccount(c *gin.Context) {
	userID := middleware.GetUserID(c)
	id := c.Param("id")
	var req models.AccountReq
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"code": 400, "msg": "参数错误"})
		return
	}

	_, err := config.Session.Update("accounts").
		Set("name", req.Name).
		Set("type", req.Type).
		Set("balance", req.Balance).
		Set("icon", req.Icon).
		Where("id = ? AND user_id = ?", id, userID).Exec()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"code": 500, "msg": "更新失败"})
		return
	}
	c.JSON(http.StatusOK, gin.H{"code": 200, "msg": "更新成功"})
}

func DeleteAccount(c *gin.Context) {
	userID := middleware.GetUserID(c)
	id := c.Param("id")

	_, err := config.Session.DeleteFrom("accounts").
		Where("id = ? AND user_id = ?", id, userID).Exec()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"code": 500, "msg": "删除失败"})
		return
	}
	c.JSON(http.StatusOK, gin.H{"code": 200, "msg": "删除成功"})
}
