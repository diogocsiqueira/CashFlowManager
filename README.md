<h2 align="center">CashFlow Manager - Backend API</h2>

<p align="center">
  API RESTful para gerenciamento financeiro pessoal multiusuário, com foco em automação de regras de negócio, processamento mensal de dados e autenticação segura.  
Projetada para suportar aplicações em produção com alta confiabilidade, escalabilidade e evolução contínua para modelo SaaS.
</p>

---

## Problema

O controle financeiro pessoal costuma falhar não apenas na interface, mas na **falta de uma base backend estruturada**, capaz de:

- lidar com múltiplos usuários simultaneamente  
- automatizar regras de negócio (ex: contas fixas)  
- garantir consistência nos dados financeiros  
- oferecer segurança adequada na autenticação  

Sem isso, sistemas financeiros se tornam frágeis, inconsistentes e difíceis de evoluir.

---

## Solução

Desenvolvi uma API robusta utilizando **Java + Spring Boot**, estruturada para suportar:

- múltiplos usuários com isolamento de dados  
- processamento mensal de transações financeiras  
- automação de contas recorrentes  
- cálculo de saldo consolidado em tempo real  
- autenticação segura baseada em JWT com refresh token  

A arquitetura foi projetada para ser **base de evolução para um SaaS financeiro**, com separação clara de responsabilidades e escalabilidade.

---

## Funcionalidades

- Autenticação completa (login, refresh, logout)
- Suporte a múltiplos usuários
- Gestão de transações financeiras por mês
- Controle de contas fixas recorrentes
- Checklist mensal de pagamentos
- Cálculo automático de saldo mensal
- Processamento de dados em tempo real
- API desacoplada pronta para múltiplos clientes (web/mobile)

---

## Arquitetura e Decisões Técnicas

- Arquitetura em camadas:
  - **Controller → Service → Domain**
- API REST organizada por contexto de domínio
- Modelagem temporal com `YearMonth` para controle financeiro mensal
- Persistência com **PostgreSQL**
- Autenticação baseada em:
  - Access Token (stateless)
  - Refresh Token armazenado em cookie HttpOnly
- Configuração de cookies com:
  - `Secure`
  - `SameSite`
  - `HttpOnly`
- Estrutura preparada para escalabilidade e evolução para SaaS
- Validação de dados com Bean Validation (Jakarta Validation)

---

## Endpoints

### Autenticação
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`

---

### Transações
- `POST /api/transactions`
- `GET /api/transactions?month=YYYY-MM`

---

### Contas Fixas
- `GET /api/fixed-bills`
- `POST /api/fixed-bills`

---

###  Controle Mensal
- `GET /api/months/{month}/fixed-bills`
- `POST /api/months/{month}/fixed-bills/{billId}/pay`
- `POST /api/months/{month}/fixed-bills/{billId}/unpay`

---

###  Resumo Financeiro
- `GET /summary/balance?month=YYYY-MM`

---

##  Integração

Esta API é utilizada por um frontend em React, consumindo os dados para exibição e interação do usuário.

 Frontend: [Link](https://github.com/diogocsiqueira/caixa-front)  
 Aplicação em produção: [Link](https://caixa-front-mu.vercel.app)

> Obs: pode haver latência na primeira requisição devido ao uso de infraestrutura gratuita.

---

## Impacto

- Sistema em produção com uso real e contínuo
- Suporte a múltiplos usuários com isolamento de dados
- Redução significativa de processos manuais no controle financeiro
- Base arquitetural preparada para evolução para SaaS
- Melhoria na consistência e confiabilidade dos dados financeiros

---

## Tecnologias

- Java
- Spring Boot
- Spring Security
- JPA / Hibernate
- PostgreSQL
- JWT (Access + Refresh Token)
- Docker
- Linux

---

## Próximos Passos

- Implementação de categorias e relatórios avançados
- Rate limiting e melhorias de segurança
- Integração com APIs externas (Open Banking)
- Evolução para arquitetura orientada a microserviços (se necessário)
