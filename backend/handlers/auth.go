package handlers

import (
	"net/http"

	"ledger-origin/config"
	"ledger-origin/middleware"
	"ledger-origin/models"

	"github.com/gin-gonic/gin"
	"golang.org/x/crypto/bcrypt"
)

func Register(c *gin.Context) {
	var req models.RegisterReq
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"code": 400, "msg": "参数错误: " + err.Error()})
		return
	}

	// 检查用户名是否已存在
	var count int
	config.Session.Select("COUNT(*)").From("users").
		Where("username = ?", req.Username).LoadOne(&count)
	if count > 0 {
		c.JSON(http.StatusBadRequest, gin.H{"code": 400, "msg": "用户名已存在"})
		return
	}

	// 加密密码
	hashedPwd, err := bcrypt.GenerateFromPassword([]byte(req.Password), bcrypt.DefaultCost)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"code": 500, "msg": "系统错误"})
		return
	}

	// 插入用户
	result, err := config.Session.InsertInto("users").
		Columns("username", "password", "nickname").
		Values(req.Username, string(hashedPwd), req.Nickname).
		Exec()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"code": 500, "msg": "注册失败"})
		return
	}

	userID, _ := result.LastInsertId()

	// 创建默认分类
	createDefaultCategories(userID)
	// 创建默认账户
	createDefaultAccount(userID)

	token, _ := middleware.GenerateToken(userID, req.Username)
	c.JSON(http.StatusOK, gin.H{"code": 200, "msg": "注册成功", "data": gin.H{
		"token":    token,
		"user_id":  userID,
		"nickname": req.Nickname,
	}})
}

func Login(c *gin.Context) {
	var req models.LoginReq
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"code": 400, "msg": "参数错误"})
		return
	}

	var user models.User
	err := config.Session.Select("*").From("users").
		Where("username = ?", req.Username).LoadOne(&user)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"code": 400, "msg": "用户名或密码错误"})
		return
	}

	if err := bcrypt.CompareHashAndPassword([]byte(user.Password), []byte(req.Password)); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"code": 400, "msg": "用户名或密码错误"})
		return
	}

	token, _ := middleware.GenerateToken(user.ID, user.Username)
	c.JSON(http.StatusOK, gin.H{"code": 200, "msg": "登录成功", "data": gin.H{
		"token":    token,
		"user_id":  user.ID,
		"nickname": user.Nickname,
		"avatar":   user.Avatar,
	}})
}

func GetProfile(c *gin.Context) {
	userID := middleware.GetUserID(c)
	var user models.User
	err := config.Session.Select("id, username, nickname, avatar, created_at").
		From("users").Where("id = ?", userID).LoadOne(&user)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"code": 404, "msg": "用户不存在"})
		return
	}
	c.JSON(http.StatusOK, gin.H{"code": 200, "data": user})
}

func createDefaultCategories(userID int64) {
	// 支出分类
	expenses := [][]string{
		{"餐饮", "food"}, {"交通", "transport"}, {"购物", "shopping"},
		{"娱乐", "entertainment"}, {"居住", "housing"}, {"医疗", "medical"},
		{"教育", "education"}, {"通讯", "communication"},
	}
	for i, cat := range expenses {
		config.Session.InsertInto("categories").
			Columns("user_id", "name", "icon", "type", "sort_order").
			Values(userID, cat[0], cat[1], 1, i+1).Exec()
	}
	// 收入分类
	incomes := [][]string{
		{"工资", "salary"}, {"奖金", "bonus"}, {"投资", "investment"}, {"兼职", "parttime"},
	}
	for i, cat := range incomes {
		config.Session.InsertInto("categories").
			Columns("user_id", "name", "icon", "type", "sort_order").
			Values(userID, cat[0], cat[1], 2, i+1).Exec()
	}
}

func createDefaultAccount(userID int64) {
	config.Session.InsertInto("accounts").
		Columns("user_id", "name", "type", "balance", "icon").
		Values(userID, "现金", "cash", 0, "cash").Exec()
	config.Session.InsertInto("accounts").
		Columns("user_id", "name", "type", "balance", "icon").
		Values(userID, "支付宝", "alipay", 0, "alipay").Exec()
	config.Session.InsertInto("accounts").
		Columns("user_id", "name", "type", "balance", "icon").
		Values(userID, "微信", "wechat", 0, "wechat").Exec()
}
