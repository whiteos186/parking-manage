# customer-mainline

Execute the parking customer archive mainline implementation plan end-to-end.

Read `docs/superpowers/plans/2026-04-08-parking-customer-mainline.md` in full first, then work through every task and step in order. After finishing each task's steps, run the Maven test command specified in that task to verify the result before moving on to the next task.

## What this plan delivers

- A `parking_customer` table as the business identity for all parking operations
- `customer_id` FK replaces `user_id` across `parking_user_vehicle`, `parking_monthly_order`, `parking_membership_order`, `parking_temp_order`, `parking_payment_record`
- Full CRUD backend: `ParkingCustomer` domain, mapper, service, controller
- Frontend customer archive page at `parking/customer/index`
- Customer selector dropdowns in vehicle, monthly, membership, temp-order, and payment views (replacing raw user ID inputs)
- Menu entry for 客户档案 in parking_menu.sql

## Execution rules

1. Use the `parking-developer` agent for all code tasks — it knows the conventions
2. Track each `- [ ]` step: mark it done after you complete it
3. Run the specified test command after each task; do not proceed past a red build
4. Do not commit unless explicitly asked — just leave the changes staged or unstaged
5. If a step's test fails unexpectedly, stop and report what failed and why before retrying

Start now by reading the plan document and then executing Task 1.
