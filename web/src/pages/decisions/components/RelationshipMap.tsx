import {
  Background,
  Controls,
  Handle,
  MarkerType,
  MiniMap,
  type NodeProps,
  Position,
  ReactFlow,
} from "@xyflow/react";
import { useMemo, useState } from "react";
import {
  RELATIONSHIP_MAP_EDGES,
  RELATIONSHIP_MAP_NODES,
} from "../relationshipMap.mocks";
import type {
  RelationshipCardNode,
  RelationshipColumnNode,
  RelationshipMapEdge,
  RelationshipMapNode,
} from "../relationshipMap.types";

const kindLabel = {
  context: "원문 맥락",
  reason: "결정 근거",
  decision: "결정 사항",
  application: "코드 반영",
} as const;

const kindClassName = {
  context: "border-blue-200 bg-blue-50",
  reason: "border-purple-200 bg-purple-50",
  decision: "border-(--color-primary-200) bg-(--color-bg-brand-subtle)",
  application: "border-green-100 bg-green-50",
} as const;

const RelationshipCard = ({
  data,
  selected,
}: NodeProps<RelationshipCardNode>) => {
  return (
    <article
      className={`w-[280px] cursor-pointer rounded-2xl border p-4 shadow-sm transition-all duration-150 hover:-translate-y-1 hover:border-(--color-primary-400) hover:shadow-md ${kindClassName[data.kind]} ${
        data.isRelated ? "border-(--color-primary-400) shadow-md" : ""
      } ${
        selected ? "ring-2 ring-(--color-action-primary) ring-offset-2" : ""
      }`}
    >
      <div className="mb-3 flex items-center justify-between gap-3">
        <span className="rounded-full bg-white/80 px-2 py-1 typo-caption1 text-(--color-text-secondary)">
          {kindLabel[data.kind]}
        </span>
        <span className="typo-caption1 text-(--color-text-tertiary)">
          {data.meta}
        </span>
      </div>
      <h2 className="typo-subtitle4 text-(--color-text-primary)">
        {data.title}
      </h2>
      <p className="mt-2 line-clamp-2 typo-body6 text-(--color-text-secondary)">
        {data.description}
      </p>
      <Handle
        type="target"
        position={Position.Left}
        className="!size-3 !border-2 !border-white !bg-(--color-primary-400)"
      />
      <Handle
        type="source"
        position={Position.Right}
        className="!size-3 !border-2 !border-white !bg-(--color-primary-400)"
      />
    </article>
  );
};

const RelationshipColumn = ({ data }: NodeProps<RelationshipColumnNode>) => {
  return (
    <div className="w-[280px] border-b border-(--color-border-divider) pb-3">
      <p className="typo-subtitle4 text-(--color-text-primary)">{data.title}</p>
      <p className="mt-1 typo-caption1 text-(--color-text-tertiary)">
        {data.description}
      </p>
    </div>
  );
};

const nodeTypes = {
  relationshipCard: RelationshipCard,
  relationshipColumn: RelationshipColumn,
};

const getDownstreamPath = (startNodeId: string | null) => {
  const nodeIds = new Set<string>();
  const edgeIds = new Set<string>();

  if (startNodeId == null) return { nodeIds, edgeIds };

  const pendingNodeIds = [startNodeId];
  nodeIds.add(startNodeId);

  while (pendingNodeIds.length > 0) {
    const currentNodeId = pendingNodeIds.shift();
    if (currentNodeId == null) continue;

    for (const edge of RELATIONSHIP_MAP_EDGES) {
      if (edge.source !== currentNodeId) continue;

      edgeIds.add(edge.id);
      const connectedNodeId = edge.target;

      if (nodeIds.has(connectedNodeId)) continue;
      nodeIds.add(connectedNodeId);
      pendingNodeIds.push(connectedNodeId);
    }
  }

  return { nodeIds, edgeIds };
};

const RelationshipMap = () => {
  const [selectedNodeId, setSelectedNodeId] = useState<string | null>(null);
  const downstreamPath = useMemo(
    () => getDownstreamPath(selectedNodeId),
    [selectedNodeId],
  );

  const edges = useMemo<RelationshipMapEdge[]>(
    () =>
      RELATIONSHIP_MAP_EDGES.map((edge) => ({
        ...edge,
        markerEnd: {
          type: MarkerType.ArrowClosed,
          color: downstreamPath.edgeIds.has(edge.id)
            ? "var(--color-primary-500)"
            : "var(--color-primary-100)",
        },
        style: {
          stroke: downstreamPath.edgeIds.has(edge.id)
            ? "var(--color-primary-500)"
            : "var(--color-primary-100)",
          strokeWidth: downstreamPath.edgeIds.has(edge.id) ? 2.5 : 1.5,
        },
      })),
    [downstreamPath.edgeIds],
  );

  const nodes = useMemo(
    () =>
      RELATIONSHIP_MAP_NODES.map((node) => {
        if (node.type !== "relationshipCard") return node;

        return {
          ...node,
          data: {
            ...node.data,
            isRelated: downstreamPath.nodeIds.has(node.id),
          },
          selected: node.id === selectedNodeId,
        };
      }),
    [downstreamPath.nodeIds, selectedNodeId],
  );

  return (
    <div className="h-full min-h-[620px] overflow-hidden rounded-3xl border border-(--color-border-default) bg-white shadow-sm">
      <ReactFlow<RelationshipMapNode, RelationshipMapEdge>
        nodes={nodes}
        edges={edges}
        nodeTypes={nodeTypes}
        nodesDraggable={false}
        nodesConnectable={false}
        edgesReconnectable={false}
        panOnDrag
        minZoom={0.5}
        maxZoom={1.5}
        fitView
        fitViewOptions={{ padding: 0.15 }}
        onNodeClick={(_, node) => {
          if (node.type !== "relationshipCard") return;
          setSelectedNodeId((currentNodeId) =>
            currentNodeId === node.id ? null : node.id,
          );
        }}
        onPaneClick={() => setSelectedNodeId(null)}
      >
        <Background color="var(--color-border-divider)" gap={20} size={1} />
        <Controls showInteractive={false} />
        <MiniMap
          pannable
          zoomable
          nodeColor={(node) => {
            if (node.type === "relationshipColumn") return "transparent";
            return "var(--color-primary-300)";
          }}
          className="!rounded-xl !border !border-(--color-border-default) !bg-white"
        />
      </ReactFlow>
    </div>
  );
};

export default RelationshipMap;
