# AWS Free Tier Ops

## Network/Security
- Use SSM Session Manager (no bastion).
- Close port 22 in EC2 Security Group.
- Expose only 8080 to ALB/CloudFront or your IP.
- RDS SG allows only EC2 SG on port 5432.
- RDS must be private (no public access).

## DB Access (Local)
- Use SSM port forwarding through EC2 to reach RDS.

## Logging (CloudWatch)
- CloudWatch Agent reads local log file.
- Retention set to 3 days.
- Framework logs set to WARN.

## CI/CD
- GitHub Actions builds and deploys to EC2 (artifact + systemd restart).
