package routes

import (
	"ledger-origin/handlers"
	"ledger-origin/middleware"

	"github.com/gin-gonic/gin"
)

func SetupRouter() *gin.Engine {
	r := gin.Default()

	// 允许跨域
	r.Use(func(c *gin.Context) {
		c.Header("Access-Control-Allow-Origin", "*")
		c.Header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
		c.Header("Access-Control-Allow-Headers", "Content-Type, Authorization")
		if c.Request.Method == "OPTIONS" {
			c.AbortWithStatus(204)
			return
		}
		c.Next()
	})

	// 公开接口
	public := r.Group("/api")
	{
		public.POST("/register", handlers.Register)
		public.POST("/login", handlers.Login)
	}

	// 需要认证的接口
	auth := r.Group("/api")
	auth.Use(middleware.AuthMiddleware())
	{
		// 用户
		auth.GET("/profile", handlers.GetProfile)

		// 交易记录
		auth.GET("/transactions", handlers.GetTransactions)
		auth.POST("/transactions", handlers.CreateTransaction)
		auth.PUT("/transactions/:id", handlers.UpdateTransaction)
		auth.DELETE("/transactions/:id", handlers.DeleteTransaction)

		// 分类
		auth.GET("/categories", handlers.GetCategories)
		auth.POST("/categories", handlers.CreateCategory)
		auth.PUT("/categories/:id", handlers.UpdateCategory)
		auth.DELETE("/categories/:id", handlers.DeleteCategory)

		// 账户
		auth.GET("/accounts", handlers.GetAccounts)
		auth.POST("/accounts", handlers.CreateAccount)
		auth.PUT("/accounts/:id", handlers.UpdateAccount)
		auth.DELETE("/accounts/:id", handlers.DeleteAccount)

		// 预算
		auth.GET("/budgets", handlers.GetBudgets)
		auth.POST("/budgets", handlers.CreateBudget)
		auth.PUT("/budgets/:id", handlers.UpdateBudget)
		auth.DELETE("/budgets/:id", handlers.DeleteBudget)

		// 统计
		auth.GET("/stats/overview", handlers.GetStatOverview)
		auth.GET("/stats/category", handlers.GetStatByCategory)
		auth.GET("/stats/daily", handlers.GetStatDaily)
	}

	return r
}
