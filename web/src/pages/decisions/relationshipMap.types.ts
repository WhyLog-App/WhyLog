import type { Edge, Node } from "@xyflow/react";

export type RelationshipNodeKind =
  | "context"
  | "reason"
  | "decision"
  | "application";

export interface RelationshipCardData extends Record<string, unknown> {
  kind: RelationshipNodeKind;
  title: string;
  description: string;
  meta: string;
  isRelated?: boolean;
}

export interface RelationshipColumnData extends Record<string, unknown> {
  title: string;
  description: string;
}

export type RelationshipCardNode = Node<
  RelationshipCardData,
  "relationshipCard"
>;

export type RelationshipColumnNode = Node<
  RelationshipColumnData,
  "relationshipColumn"
>;

export type RelationshipMapNode = RelationshipCardNode | RelationshipColumnNode;

export type RelationshipMapEdge = Edge<{ relation: string }>;
