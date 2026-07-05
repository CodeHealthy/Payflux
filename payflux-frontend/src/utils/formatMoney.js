export function formatMoney(amount, currency = 'PKR') {
  const numericAmount = Number(amount || 0)

  return new Intl.NumberFormat('en-PK', {
    style: 'currency',
    currency,
    maximumFractionDigits: 2,
  }).format(numericAmount)
}
