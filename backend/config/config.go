package config

import (
	"fmt"
	"log"
	"os"
	"strconv"

	"github.com/gocraft/dbr/v2"
)

var Session *dbr.Session

type DBConfig struct {
	Host     string
	Port     int
	User     string
	Password string
	DBName   string
}

// DefaultConfig 优先从环境变量读取，方便部署且避免在代码中硬编码敏感信息。
// 可设置 DB_HOST / DB_PORT / DB_USER / DB_PASSWORD / DB_NAME。
func DefaultConfig() DBConfig {
	port, _ := strconv.Atoi(getEnv("DB_PORT", "3306"))
	return DBConfig{
		Host:     getEnv("DB_HOST", "127.0.0.1"),
		Port:     port,
		User:     getEnv("DB_USER", "root"),
		Password: getEnv("DB_PASSWORD", "root"),
		DBName:   getEnv("DB_NAME", "ledger_origin"),
	}
}

func getEnv(key, fallback string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return fallback
}

func InitDB(cfg DBConfig) {
	// 注意：不开启 parseTime，让 DATE/DATETIME 列按字符串扫描，
	// 避免 dbr 在 SELECT * 时把 time 列扫进 string 字段而整批 Load 失败。
	dsn := fmt.Sprintf("%s:%s@tcp(%s:%d)/%s?charset=utf8mb4&loc=Local",
		cfg.User, cfg.Password, cfg.Host, cfg.Port, cfg.DBName)

	conn, err := dbr.Open("mysql", dsn, nil)
	if err != nil {
		log.Fatalf("数据库连接失败: %v", err)
	}

	conn.SetMaxOpenConns(20)
	conn.SetMaxIdleConns(10)

	Session = conn.NewSession(nil)

	if err := conn.Ping(); err != nil {
		log.Fatalf("数据库Ping失败: %v", err)
	}

	log.Println("数据库连接成功")
}
