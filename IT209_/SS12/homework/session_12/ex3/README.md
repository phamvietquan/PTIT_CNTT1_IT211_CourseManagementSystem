# Bai 3: Tach biet luong CI/CD da moi truong (Staging & Production)

## Mo ta

Workflow `cd-multi-env.yml` quan ly viec trien khai len 2 moi truong rieng biet thong qua **GitHub Environments**:

| Moi truong | Nhanh kich hoat | Phe duyet |
|---|---|---|
| **staging** | `develop` | Tu dong (khong can phe duyet) |
| **production** | `main` | **Yeu cau phe duyet thu cong** |

---

## Cau truc file

```
.github/
+-- workflows/
|   +-- cd-multi-env.yml          # Workflow chinh
IT209_/SS12/homework/session_12/ex3/
+-- README.md                      # Tai lieu nay
```

---

## Huong dan thiet lap GitHub Environments

### Buoc 1: Tao 2 Environments tren GitHub

1. Vao **Settings** -> **Environments** trong repository
2. Bam **New environment** -> Dat ten `staging` -> **Configure environment**
3. Bam **New environment** -> Dat ten `production` -> **Configure environment**

### Buoc 2: Bat Required Reviewers cho Production

1. Vao **Settings** -> **Environments** -> chon `production`
2. Tick chon **Required reviewers**
3. Tim va chon **tai khoan cua ban** lam nguoi duyet
4. Bam **Save protection rules**

```
Settings
+-- Environments
    +-- staging          (khong can cai them)
    +-- production
        [x] Required reviewers
            - @phamvietquan   <- them tai khoan cua ban
        [Save protection rules]
```

---

## Luong hoat dong

### Push vao `develop` -> Deploy Staging (tu dong)

```
git checkout develop
git push origin develop
        |
        v
  GitHub Actions kich hoat cd-multi-env.yml
        |
        v
  Job: deploy-staging
  [Checkout] -> [Login GHCR] -> [Build Image] -> [Push :staging] -> [Deploy]
        |
        v
  HOAN THANH (xanh la)
```

### Push vao `main` -> Deploy Production (can phe duyet)

```
git checkout main
git push origin main
        |
        v
  GitHub Actions kich hoat cd-multi-env.yml
        |
        v
  Job: deploy-prod
  [WAITING FOR REVIEW]  <-- Dung o day, gui thong bao cho reviewer
        |
        v  (reviewer bam "Approve and deploy")
  [Checkout] -> [Login GHCR] -> [Build Image] -> [Push :latest] -> [Deploy]
        |
        v
  HOAN THANH (xanh la)
```

---

## Giai thich cac thanh phan chinh

### 1. Khai bao Environment trong Job

```yaml
jobs:
  deploy-staging:
    environment:
      name: staging
      url: https://staging.example.com   # Hien thi link moi truong

  deploy-prod:
    environment:
      name: production                   # Ten phai trung voi ten tren GitHub Settings
      url: https://api.prod.example.com
```

### 2. Dieu kien chay (if condition)

```yaml
# Job staging chi chay khi push vao develop
deploy-staging:
  if: github.ref == 'refs/heads/develop'

# Job production chi chay khi push vao main
deploy-prod:
  if: github.ref == 'refs/heads/main'
```

### 3. workflow_dispatch (chay thu cong)

Workflow ho tro chay thu cong tu tab Actions voi tuy chon chon moi truong:

```yaml
on:
  workflow_dispatch:
    inputs:
      environment:
        type: choice
        options: [staging, production]
```

---

## Cach kiem tra

### Test Staging:
```bash
git checkout develop
# Sua mot file bat ky
echo "test" >> test.txt
git add test.txt
git commit -m "test: trigger staging deploy"
git push origin develop
```

### Test Production (voi phe duyet):
```bash
git checkout main
git merge develop
git push origin main
# -> Vao tab Actions -> thay "Waiting for review"
# -> Bam "Review deployments" -> "Approve and deploy"
```

---

## Luu y quan trong

> **GITHUB_TOKEN** mac dinh da co quyen doc/ghi Packages, khong can tao secret thu cong.

> Environment `production` phai duoc tao tren **GitHub Settings** truoc khi workflow chay,
> neu khong step "Waiting for review" se khong hien ra.

> Nguoi duyet **khong the tu phe duyet** commit cua chinh minh trong mot so cai dat bao mat.
