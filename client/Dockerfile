# client/Dockerfile
FROM node:20-alpine AS builder

WORKDIR /app

# Install dependencies
COPY package.json yarn.lock ./
RUN yarn install --frozen-lockfile

# Build application
COPY . .
RUN yarn build

# Production image
FROM node:20-alpine

WORKDIR /app

# Copy necessary files
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public
COPY --from=builder /app/package.json ./
COPY --from=builder /app/yarn.lock ./
COPY --from=builder /app/next.config.js ./

# Install production dependencies
RUN yarn install --frozen-lockfile --production

EXPOSE 3000
ENV NODE_ENV=production
CMD ["yarn", "start"]
