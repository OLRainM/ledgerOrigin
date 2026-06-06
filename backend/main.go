package main

import (
	"log"

	"ledger-origin/config"
	"ledger-origin/routes"

	_ "github.com/go-sql-driver/mysql"
)

func main() {
	// 初始化数据库
	cfg := config.DefaultConfig()
	config.InitDB(cfg)

	// 启动路由
	r := routes.SetupRouter()

	log.Println("服务启动在 :8080")
	if err := r.Run(":8080"); err != nil {
		log.Fatalf("服务启动失败: %v", err)
	}
}
