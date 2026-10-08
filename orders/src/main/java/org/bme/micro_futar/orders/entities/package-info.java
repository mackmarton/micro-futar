/**
 * Soft-deleted rows (deleted = true) are hidden from queries (findAll, derived queries, counts),
 * but still loadable by id (findById, merge on upsert), so historic references keep resolving.
 * Note: existsById() is a query, so it returns false for soft-deleted rows.
 */
@FilterDef(name = "notDeleted", defaultCondition = "deleted = false", autoEnabled = true)
package org.bme.micro_futar.orders.entities;

import org.hibernate.annotations.FilterDef;
